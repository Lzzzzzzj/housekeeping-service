package com.example.back.service.impl;

import com.example.back.common.constant.OrderStatus;
import com.example.back.dto.OrderCreateDTO;
import com.example.back.dto.OrderPayVO;
import com.example.back.entity.oms.OmsOrder;
import com.example.back.entity.oms.OmsOrderStatusLog;
import com.example.back.entity.pms.PmsService;
import com.example.back.mapper.OmsOrderMapper;
import com.example.back.mapper.OmsOrderStatusLogMapper;
import com.example.back.mapper.PmsServiceMapper;
import com.example.back.mapper.UmsMemberMapper;
import com.example.back.service.UserOrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserOrderServiceImpl implements UserOrderService {

    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderStatusLogMapper omsOrderStatusLogMapper;
    private final PmsServiceMapper pmsServiceMapper;
    private final UmsMemberMapper umsMemberMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public OmsOrder createOrder(Long memberId, OrderCreateDTO dto) {
        if (umsMemberMapper.selectById(memberId) == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        PmsService service = pmsServiceMapper.selectById(dto.getServiceId());
        if (service == null) {
            throw new IllegalArgumentException("服务不存在");
        }
        if (dto.getAppointmentTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("预约时间必须为未来时间");
        }

        String addressJson = toJson(dto.getAddressInfo());
        String extJson = dto.getExtInfo() != null ? toJson(dto.getExtInfo()) : null;

        BigDecimal totalAmount = service.getBasePrice();
        String orderSn = "HK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        OmsOrder order = new OmsOrder();
        order.setOrderSn(orderSn);
        order.setMemberId(memberId);
        order.setStaffId(null);
        order.setServiceId(service.getId());
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING_PAY);
        order.setAppointmentTime(dto.getAppointmentTime());
        order.setAddressInfo(addressJson);
        order.setExtInfo(extJson);

        omsOrderMapper.insert(order);

        OmsOrderStatusLog log = new OmsOrderStatusLog();
        log.setOrderId(order.getId());
        log.setPreStatus(null);
        log.setPostStatus(OrderStatus.PENDING_PAY);
        log.setOperator("user:" + memberId);
        log.setRemark("用户创建订单");
        omsOrderStatusLogMapper.insert(log);

        return omsOrderMapper.selectById(order.getId());
    }

    @Override
    public OrderPayVO pay(Long memberId, String orderSn) {
        OmsOrder order = omsOrderMapper.selectByOrderSn(orderSn);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("无权操作该订单");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAY) {
            throw new IllegalArgumentException("订单状态不允许支付");
        }

        // TODO: 调用微信支付 SDK 生成预支付单
        OrderPayVO vo = new OrderPayVO();
        vo.setOrderSn(orderSn);
        vo.setPrepayId("prepay_placeholder");
        vo.setPaySign("placeholder");
        vo.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
        vo.setNonceStr(UUID.randomUUID().toString().replace("-", ""));
        return vo;
    }

    @Override
    public List<OmsOrder> listOrders(Long memberId, Integer status) {
        return omsOrderMapper.selectByMemberId(memberId, status);
    }

    @Override
    @Transactional
    public void cancelOrder(Long memberId, Long orderId) {
        OmsOrder order = omsOrderMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("无权操作该订单");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("订单已取消");
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("订单已完成，无法取消");
        }

        int preStatus = order.getStatus();
        omsOrderMapper.updateStatus(orderId, OrderStatus.CANCELLED);

        OmsOrderStatusLog log = new OmsOrderStatusLog();
        log.setOrderId(orderId);
        log.setPreStatus(preStatus);
        log.setPostStatus(OrderStatus.CANCELLED);
        log.setOperator("user:" + memberId);
        log.setRemark("用户取消订单");
        omsOrderStatusLogMapper.insert(log);
    }

    @Override
    @Transactional
    public void confirmOrder(Long memberId, Long orderId) {
        OmsOrder order = omsOrderMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("无权操作该订单");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("订单已取消，无法确认完成");
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("订单已完成");
        }
        if (order.getStatus() != OrderStatus.PENDING_SETTLE) {
            throw new IllegalArgumentException("当前订单状态不允许确认完成");
        }

        int preStatus = order.getStatus();
        omsOrderMapper.updateStatus(orderId, OrderStatus.COMPLETED);

        OmsOrderStatusLog log = new OmsOrderStatusLog();
        log.setOrderId(orderId);
        log.setPreStatus(preStatus);
        log.setPostStatus(OrderStatus.COMPLETED);
        log.setOperator("user:" + memberId);
        log.setRemark("用户确认完成");
        omsOrderStatusLogMapper.insert(log);
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败", e);
        }
    }
}
