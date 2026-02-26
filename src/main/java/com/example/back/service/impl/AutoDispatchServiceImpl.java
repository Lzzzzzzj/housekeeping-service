package com.example.back.service.impl;

import com.example.back.common.constant.OrderStatus;
import com.example.back.entity.oms.OmsOrder;
import com.example.back.entity.oms.OmsOrderStatusLog;
import com.example.back.entity.sys.SysConfig;
import com.example.back.entity.ums.UmsStaff;
import com.example.back.mapper.OmsOrderMapper;
import com.example.back.mapper.OmsOrderStatusLogMapper;
import com.example.back.mapper.SysConfigMapper;
import com.example.back.mapper.UmsStaffMapper;
import com.example.back.service.OrderGrabPoolService;
import com.example.back.service.RedisLockService;
import com.example.back.service.AutoDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 自动派单：按可配置权重（评分、距离、准时率）对候选师傅排序，择优分配。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoDispatchServiceImpl implements AutoDispatchService {

    private static final String GEO_KEY_STAFF_LOCATIONS = "staff:locations";
    private static final String KEY_ENABLE = "dispatch.enable_auto";
    private static final String KEY_WEIGHT_SCORE = "dispatch.weight.score";
    private static final String KEY_WEIGHT_DISTANCE = "dispatch.weight.distance";
    private static final String KEY_WEIGHT_PUNCTUAL = "dispatch.weight.punctual";
    private static final String KEY_MAX_CONCURRENT = "dispatch.max_concurrent_orders";
    private static final String KEY_RADIUS_KM = "dispatch.radius_km";
    private static final double DEFAULT_WEIGHT_SCORE = 0.5;
    private static final double DEFAULT_WEIGHT_DISTANCE = 0.3;
    private static final double DEFAULT_WEIGHT_PUNCTUAL = 0.2;
    private static final int DEFAULT_MAX_CONCURRENT = 3;
    private static final int CANDIDATE_LIMIT = 50;
    private static final double DEFAULT_RADIUS_KM = 5.0;

    private final SysConfigMapper sysConfigMapper;
    private final UmsStaffMapper umsStaffMapper;
    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderStatusLogMapper omsOrderStatusLogMapper;
    private final RedisLockService redisLockService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final OrderGrabPoolService orderGrabPoolService;

    @Override
    @Transactional
    public void tryAutoAssign(Long orderId) {
        if (orderId == null) return;
        String lockToken = redisLockService.tryLock("order:" + orderId, java.time.Duration.ofSeconds(5));
        if (lockToken == null) {
            log.debug("自动派单获取分布式锁失败，可能有并行抢单，orderId={}", orderId);
            return;
        }
        try {
            OmsOrder order = omsOrderMapper.selectById(orderId);
            if (order == null) return;
            if (order.getStatus() == null || order.getStatus() != OrderStatus.PENDING_ACCEPT) return;
            if (order.getStaffId() != null) return;

            if (!isAutoDispatchEnabled()) {
                log.debug("自动派单已关闭，跳过 orderId={}", orderId);
                return;
            }

            double ws = getConfigDouble(KEY_WEIGHT_SCORE, DEFAULT_WEIGHT_SCORE);
            double wd = getConfigDouble(KEY_WEIGHT_DISTANCE, DEFAULT_WEIGHT_DISTANCE);
            double wp = getConfigDouble(KEY_WEIGHT_PUNCTUAL, DEFAULT_WEIGHT_PUNCTUAL);
            int maxConcurrent = getConfigInt(KEY_MAX_CONCURRENT, DEFAULT_MAX_CONCURRENT);
            double radiusKm = getConfigDouble(KEY_RADIUS_KM, DEFAULT_RADIUS_KM);

            List<UmsStaff> candidates = umsStaffMapper.selectAutoAcceptCandidates(CANDIDATE_LIMIT);
            if (candidates.isEmpty()) {
                log.debug("无自动接单候选师傅，orderId={}", orderId);
                return;
            }

            List<ScoredStaff> scored = new ArrayList<>();
            for (UmsStaff s : candidates) {
                int inProgress = omsOrderMapper.countInProgressByStaffId(s.getId());
                if (inProgress >= maxConcurrent) continue;

                double S = normalizeScore(s.getServiceScore());
                double D = distanceScore(s.getId(), order, radiusKm);
                double P = punctualScore(s);
                double W = ws * S + wd * D + wp * P;
                scored.add(new ScoredStaff(s, W));
            }

            scored.sort(Comparator.comparingDouble(ScoredStaff::score).reversed());

            for (ScoredStaff ss : scored) {
                int affected = omsOrderMapper.grabOrder(orderId, ss.staff.getId(), OrderStatus.PENDING_ACCEPT);
                if (affected > 0) {
                    // 自动派单成功后，从抢单池中移除订单
                    orderGrabPoolService.removeFromPool(orderId);

                    OmsOrderStatusLog logEntry = new OmsOrderStatusLog();
                    logEntry.setOrderId(orderId);
                    logEntry.setPreStatus(OrderStatus.PENDING_ACCEPT);
                    logEntry.setPostStatus(OrderStatus.PENDING_SERVICE);
                    logEntry.setOperator("system:auto_dispatch");
                    logEntry.setRemark("系统自动派单，分配给师傅ID=" + ss.staff.getId());
                    omsOrderStatusLogMapper.insert(logEntry);
                    log.info("自动派单成功 orderId={} staffId={}", orderId, ss.staff.getId());
                    return;
                }
            }
            log.debug("自动派单未匹配到可用师傅 orderId={}", orderId);
        } finally {
            redisLockService.unlock("order:" + orderId, lockToken);
        }
    }

    private boolean isAutoDispatchEnabled() {
        SysConfig c = sysConfigMapper.selectByKey(KEY_ENABLE);
        if (c == null || c.getConfigValue() == null) return true;
        return "true".equalsIgnoreCase(c.getConfigValue().trim());
    }

    private double getConfigDouble(String key, double defaultVal) {
        SysConfig c = sysConfigMapper.selectByKey(key);
        if (c == null || c.getConfigValue() == null) return defaultVal;
        try {
            return Double.parseDouble(c.getConfigValue().trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private int getConfigInt(String key, int defaultVal) {
        SysConfig c = sysConfigMapper.selectByKey(key);
        if (c == null || c.getConfigValue() == null) return defaultVal;
        try {
            return Integer.parseInt(c.getConfigValue().trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    /** 评分 0-5 归一化到 0-1 */
    private double normalizeScore(BigDecimal score) {
        if (score == null) return 0.2;
        double v = score.doubleValue();
        if (v <= 0) return 0;
        return Math.min(1.0, v / 5.0);
    }

    /**
     * 距离得分：基于 Redis GEO 中师傅位置 + 订单地址经纬度计算。
     * 按照 D = max(0, 1 - distKm / radiusKm) 归一化到 0-1。
     */
    private double distanceScore(Long staffId, OmsOrder order, double radiusKm) {
        if (order == null || order.getAddressInfo() == null || radiusKm <= 0) {
            return 0.0;
        }
        try {
            JsonNode node = objectMapper.readTree(order.getAddressInfo());
            JsonNode lngNode = node.get("lng");
            JsonNode latNode = node.get("lat");
            if (lngNode == null || latNode == null || !lngNode.isNumber() || !latNode.isNumber()) {
                return 0.0;
            }
            double orderLng = lngNode.asDouble();
            double orderLat = latNode.asDouble();

            GeoOperations<String, String> geoOps = stringRedisTemplate.opsForGeo();
            java.util.List<Point> positions = geoOps.position(GEO_KEY_STAFF_LOCATIONS, staffId.toString());
            if (positions == null || positions.isEmpty() || positions.get(0) == null) {
                return 0.0;
            }
            Point p = positions.get(0);
            double staffLng = p.getX();
            double staffLat = p.getY();

            double distKm = distanceKm(staffLat, staffLng, orderLat, orderLng);
            if (distKm <= 0) {
                return 1.0;
            }
            if (distKm >= radiusKm) {
                return 0.0;
            }
            double D = 1.0 - distKm / radiusKm;
            return Math.max(0.0, Math.min(1.0, D));
        } catch (Exception e) {
            return 0.0;
        }
    }

    /** 准时/履约得分：暂无历史履约数据时用接单数简单模拟，避免所有人相同 */
    private double punctualScore(UmsStaff staff) {
        int n = staff.getOrderCount() != null ? staff.getOrderCount() : 0;
        return Math.min(1.0, 0.5 + n / 20.0);
    }

    private record ScoredStaff(UmsStaff staff, double score) {}

    private double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0; // 地球半径 km
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double deltaLat = radLat2 - radLat1;
        double deltaLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
