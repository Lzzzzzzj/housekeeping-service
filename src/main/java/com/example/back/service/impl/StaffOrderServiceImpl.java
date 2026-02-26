package com.example.back.service.impl;

import com.example.back.common.constant.OrderStatus;
import com.example.back.dto.OrderActionDTO;
import com.example.back.dto.OrderExtraFeeDTO;
import com.example.back.entity.oms.OmsOrder;
import com.example.back.entity.oms.OmsOrderExtra;
import com.example.back.entity.oms.OmsOrderStatusLog;
import com.example.back.entity.ums.UmsStaff;
import com.example.back.mapper.OmsOrderExtraMapper;
import com.example.back.mapper.OmsOrderMapper;
import com.example.back.mapper.OmsOrderStatusLogMapper;
import com.example.back.mapper.UmsStaffMapper;
import com.example.back.service.RedisLockService;
import com.example.back.service.OrderGrabPoolService;
import com.example.back.service.StaffOrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 师傅端 - 订单服务实现
 */
@Service
@RequiredArgsConstructor
public class StaffOrderServiceImpl implements StaffOrderService {

    private static final String GEO_KEY_STAFF_LOCATIONS = "staff:locations";
    private static final double DEFAULT_RADIUS_KM = 5.0;

    private final UmsStaffMapper umsStaffMapper;
    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderStatusLogMapper omsOrderStatusLogMapper;
    private final OmsOrderExtraMapper omsOrderExtraMapper;
    private final ObjectMapper objectMapper;
    private final RedisLockService redisLockService;
    private final StringRedisTemplate stringRedisTemplate;
    private final OrderGrabPoolService orderGrabPoolService;

    @Override
    @Transactional
    public void grabOrder(Long staffUserId, Long orderId) {
        UmsStaff staff = requireStaff(staffUserId);
        if (orderId == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }

        String lockToken = redisLockService.tryLock("order:" + orderId, java.time.Duration.ofSeconds(5));
        if (lockToken == null) {
            throw new IllegalStateException("抢单人数过多，请稍后重试");
        }
        try {
            OmsOrder order = omsOrderMapper.selectById(orderId);
            if (order == null) {
                throw new IllegalArgumentException("订单不存在");
            }
            if (order.getStatus() == null || !order.getStatus().equals(OrderStatus.PENDING_ACCEPT)) {
                throw new IllegalArgumentException("当前订单状态不允许接单");
            }

            int affected = omsOrderMapper.grabOrder(orderId, staff.getId(), OrderStatus.PENDING_ACCEPT);
            if (affected == 0) {
                throw new IllegalStateException("抢单失败，订单可能已被其他服务人员接走");
            }

            // 抢单成功后从抢单池中移除该订单
            orderGrabPoolService.removeFromPool(orderId);

            OmsOrderStatusLog log = new OmsOrderStatusLog();
            log.setOrderId(orderId);
            log.setPreStatus(order.getStatus());
            log.setPostStatus(OrderStatus.PENDING_SERVICE);
            log.setOperator("staff_user:" + staffUserId);
            log.setRemark("服务人员接单");
            omsOrderStatusLogMapper.insert(log);
        } finally {
            redisLockService.unlock("order:" + orderId, lockToken);
        }
    }

    @Override
    public List<OmsOrder> listGrabPool(Long staffUserId) {
        UmsStaff staff = requireStaff(staffUserId);

        // 1. 优先从 Redis 抢单池读取订单ID
        List<Long> ids = orderGrabPoolService.listPoolIds(100);
        List<OmsOrder> all;
        if (ids.isEmpty()) {
            // 兜底：从数据库拉取所有待接单订单
            all = omsOrderMapper.selectGrabPool(OrderStatus.PENDING_ACCEPT);
        } else {
            all = ids.stream()
                    .map(omsOrderMapper::selectById)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
        if (all.isEmpty()) {
            return all;
        }

        GeoOperations<String, String> geoOps = stringRedisTemplate.opsForGeo();
        List<Point> positions = geoOps.position(GEO_KEY_STAFF_LOCATIONS, staff.getId().toString());
        if (positions == null || positions.isEmpty() || positions.get(0) == null) {
            // 未上报位置时，返回全部待接单，保持兼容
            return all;
        }
        Point p = positions.get(0);
        double staffLng = p.getX();
        double staffLat = p.getY();

        return all.stream()
                .filter(o -> withinRadius(o, staffLat, staffLng, DEFAULT_RADIUS_KM))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void processAction(Long staffUserId, OrderActionDTO dto) {
        UmsStaff staff = requireStaff(staffUserId);
        if (dto == null || dto.getOrderId() == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }

        OmsOrder order = omsOrderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (order.getStaffId() == null || !order.getStaffId().equals(staff.getId())) {
            throw new IllegalArgumentException("只能操作自己接的订单");
        }

        String action = dto.getAction();
        if (action == null) {
            throw new IllegalArgumentException("动作不能为空");
        }
        String upperAction = action.toUpperCase();

        int preStatus = order.getStatus();
        int postStatus = preStatus;
        String remarkPrefix;

        switch (upperAction) {
            case "DEPART" -> {
                if (preStatus != OrderStatus.PENDING_SERVICE) {
                    throw new IllegalArgumentException("当前状态不能执行出发动作用");
                }
                remarkPrefix = "服务人员出发";
            }
            case "ARRIVE" -> {
                if (preStatus != OrderStatus.PENDING_SERVICE) {
                    throw new IllegalArgumentException("当前状态不能执行到达动作用");
                }
                remarkPrefix = "服务人员到达现场";
            }
            case "START" -> {
                if (preStatus != OrderStatus.PENDING_SERVICE) {
                    throw new IllegalArgumentException("当前状态不能开始服务");
                }
                postStatus = OrderStatus.IN_SERVICE;
                omsOrderMapper.updateStatus(order.getId(), postStatus);
                remarkPrefix = "开始服务";
            }
            case "FINISH" -> {
                if (preStatus != OrderStatus.IN_SERVICE) {
                    throw new IllegalArgumentException("当前状态不能完成服务");
                }
                postStatus = OrderStatus.PENDING_SETTLE;
                omsOrderMapper.updateStatus(order.getId(), postStatus);
                remarkPrefix = "服务完成";
            }
            default -> throw new IllegalArgumentException("不支持的动作类型");
        }

        StringBuilder remark = new StringBuilder(remarkPrefix);
        if (dto.getLng() != null && dto.getLat() != null) {
            remark.append("，位置[lng=").append(dto.getLng())
                    .append(", lat=").append(dto.getLat()).append("]");
        }
        if (dto.getPhotos() != null && !dto.getPhotos().isEmpty()) {
            remark.append("，照片数量=").append(dto.getPhotos().size());
        }

        OmsOrderStatusLog log = new OmsOrderStatusLog();
        log.setOrderId(order.getId());
        log.setPreStatus(preStatus);
        log.setPostStatus(postStatus);
        log.setOperator("staff_user:" + staffUserId);
        log.setRemark(remark.toString());
        omsOrderStatusLogMapper.insert(log);
    }

    @Override
    @Transactional
    public void createExtraFee(Long staffUserId, OrderExtraFeeDTO dto) {
        UmsStaff staff = requireStaff(staffUserId);
        if (dto == null || dto.getOrderId() == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        if (dto.getAmount() == null || dto.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("加价金额必须大于0");
        }

        OmsOrder order = omsOrderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (order.getStaffId() == null || !order.getStaffId().equals(staff.getId())) {
            throw new IllegalArgumentException("只能为自己接的订单发起加价");
        }

        int status = order.getStatus();
        if (status != OrderStatus.PENDING_SERVICE && status != OrderStatus.IN_SERVICE) {
            throw new IllegalArgumentException("当前订单状态不允许发起加价");
        }

        OmsOrderExtra extra = new OmsOrderExtra();
        extra.setOrderId(order.getId());
        extra.setTitle(dto.getTitle());
        extra.setAmount(dto.getAmount());
        extra.setPayStatus(0);
        if (dto.getPhotos() != null && !dto.getPhotos().isEmpty()) {
            try {
                extra.setEvidencePics(objectMapper.writeValueAsString(dto.getPhotos()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("现场照片序列化失败", e);
            }
        }
        omsOrderExtraMapper.insert(extra);

        OmsOrderStatusLog log = new OmsOrderStatusLog();
        log.setOrderId(order.getId());
        log.setPreStatus(order.getStatus());
        log.setPostStatus(order.getStatus());
        log.setOperator("staff_user:" + staffUserId);
        log.setRemark("发起现场加价: " + dto.getTitle() + ", 金额=" + dto.getAmount());
        omsOrderStatusLogMapper.insert(log);
    }

    @Override
    public void updateAutoAccept(Long staffUserId, boolean enable) {
        UmsStaff staff = requireStaff(staffUserId);
        umsStaffMapper.updateAutoAccept(staff.getId(), enable ? 1 : 0);
    }

    private UmsStaff requireStaff(Long staffUserId) {
        if (staffUserId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        UmsStaff staff = umsStaffMapper.selectByUserId(staffUserId);
        if (staff == null) {
            throw new IllegalArgumentException("当前账号尚未注册为服务人员");
        }
        if (staff.getAuditStatus() == null || staff.getAuditStatus() != 1) {
            throw new IllegalArgumentException("服务人员尚未审核通过，不能操作订单");
        }
        return staff;
    }

    private boolean withinRadius(OmsOrder order, double staffLat, double staffLng, double radiusKm) {
        if (order == null || order.getAddressInfo() == null) {
            return false;
        }
        try {
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(order.getAddressInfo());
            com.fasterxml.jackson.databind.JsonNode lngNode = node.get("lng");
            com.fasterxml.jackson.databind.JsonNode latNode = node.get("lat");
            if (lngNode == null || latNode == null || !lngNode.isNumber() || !latNode.isNumber()) {
                return false;
            }
            double orderLng = lngNode.asDouble();
            double orderLat = latNode.asDouble();
            double distKm = distanceKm(staffLat, staffLng, orderLat, orderLng);
            return distKm <= radiusKm;
        } catch (Exception e) {
            return false;
        }
    }

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

