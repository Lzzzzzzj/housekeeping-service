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
import com.example.back.service.AutoDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final String KEY_ENABLE = "dispatch.enable_auto";
    private static final String KEY_WEIGHT_SCORE = "dispatch.weight.score";
    private static final String KEY_WEIGHT_DISTANCE = "dispatch.weight.distance";
    private static final String KEY_WEIGHT_PUNCTUAL = "dispatch.weight.punctual";
    private static final String KEY_MAX_CONCURRENT = "dispatch.max_concurrent_orders";
    private static final double DEFAULT_WEIGHT_SCORE = 0.5;
    private static final double DEFAULT_WEIGHT_DISTANCE = 0.3;
    private static final double DEFAULT_WEIGHT_PUNCTUAL = 0.2;
    private static final int DEFAULT_MAX_CONCURRENT = 3;
    private static final int CANDIDATE_LIMIT = 50;

    private final SysConfigMapper sysConfigMapper;
    private final UmsStaffMapper umsStaffMapper;
    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderStatusLogMapper omsOrderStatusLogMapper;

    @Override
    @Transactional
    public void tryAutoAssign(Long orderId) {
        if (orderId == null) return;
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
            double D = distanceScore(s.getId(), order);
            double P = punctualScore(s);
            double W = ws * S + wd * D + wp * P;
            scored.add(new ScoredStaff(s, W));
        }

        scored.sort(Comparator.comparingDouble(ScoredStaff::score).reversed());

        for (ScoredStaff ss : scored) {
            int affected = omsOrderMapper.grabOrder(orderId, ss.staff.getId(), OrderStatus.PENDING_ACCEPT);
            if (affected > 0) {
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

    /** 距离得分：当前无 Redis 时统一返回 1.0；接入 GEO 后可改为 1 - distKm/radius */
    private double distanceScore(Long staffId, OmsOrder order) {
        return 1.0;
    }

    /** 准时/履约得分：暂无历史履约数据时用接单数简单模拟，避免所有人相同 */
    private double punctualScore(UmsStaff staff) {
        int n = staff.getOrderCount() != null ? staff.getOrderCount() : 0;
        return Math.min(1.0, 0.5 + n / 20.0);
    }

    private record ScoredStaff(UmsStaff staff, double score) {}
}
