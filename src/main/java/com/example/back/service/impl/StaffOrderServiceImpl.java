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
import com.example.back.service.StaffOrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 师傅端 - 订单服务实现
 */
@Service
@RequiredArgsConstructor
public class StaffOrderServiceImpl implements StaffOrderService {

    private final UmsStaffMapper umsStaffMapper;
    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderStatusLogMapper omsOrderStatusLogMapper;
    private final OmsOrderExtraMapper omsOrderExtraMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void grabOrder(Long staffUserId, Long orderId) {
        UmsStaff staff = requireStaff(staffUserId);
        if (orderId == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }

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

        OmsOrderStatusLog log = new OmsOrderStatusLog();
        log.setOrderId(orderId);
        log.setPreStatus(order.getStatus());
        log.setPostStatus(OrderStatus.PENDING_SERVICE);
        log.setOperator("staff_user:" + staffUserId);
        log.setRemark("服务人员接单");
        omsOrderStatusLogMapper.insert(log);
    }

    @Override
    public List<OmsOrder> listGrabPool(Long staffUserId) {
        // 仅校验当前用户确实是审核通过的服务人员，当前实现不做基于地理位置的过滤
        requireStaff(staffUserId);
        return omsOrderMapper.selectGrabPool(OrderStatus.PENDING_ACCEPT);
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
}

