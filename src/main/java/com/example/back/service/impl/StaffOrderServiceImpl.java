package com.example.back.service.impl;

import com.example.back.common.constant.OrderStatus;
import com.example.back.entity.oms.OmsOrder;
import com.example.back.entity.oms.OmsOrderStatusLog;
import com.example.back.entity.ums.UmsStaff;
import com.example.back.mapper.OmsOrderMapper;
import com.example.back.mapper.OmsOrderStatusLogMapper;
import com.example.back.mapper.UmsStaffMapper;
import com.example.back.service.StaffOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 师傅端 - 订单服务实现
 */
@Service
@RequiredArgsConstructor
public class StaffOrderServiceImpl implements StaffOrderService {

    private final UmsStaffMapper umsStaffMapper;
    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderStatusLogMapper omsOrderStatusLogMapper;

    @Override
    @Transactional
    public void grabOrder(Long staffUserId, Long orderId) {
        if (staffUserId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }

        UmsStaff staff = umsStaffMapper.selectByUserId(staffUserId);
        if (staff == null) {
            throw new IllegalArgumentException("当前账号尚未注册为服务人员");
        }
        if (staff.getAuditStatus() == null || staff.getAuditStatus() != 1) {
            throw new IllegalArgumentException("服务人员尚未审核通过，不能接单");
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
}

