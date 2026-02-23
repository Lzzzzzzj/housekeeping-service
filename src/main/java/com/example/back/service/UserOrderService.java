package com.example.back.service;

import com.example.back.dto.OrderCreateDTO;
import com.example.back.dto.OrderPayVO;
import com.example.back.entity.oms.OmsOrder;

import java.util.List;

public interface UserOrderService {

    /**
     * 创建订单
     */
    OmsOrder createOrder(Long memberId, OrderCreateDTO dto);

    /**
     * 发起支付 (返回预支付参数，占位实现)
     */
    OrderPayVO pay(Long memberId, String orderSn);

    /**
     * 订单列表
     */
    List<OmsOrder> listOrders(Long memberId, Integer status);

    /**
     * 取消订单
     */
    void cancelOrder(Long memberId, Long orderId);

    /**
     * 用户确认订单完成
     */
    void confirmOrder(Long memberId, Long orderId);
}
