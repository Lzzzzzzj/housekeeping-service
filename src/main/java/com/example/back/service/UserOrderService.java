package com.example.back.service;

import com.example.back.dto.OrderCreateDTO;
import com.example.back.entity.oms.OmsOrder;

import java.util.List;

public interface UserOrderService {

    /**
     * 创建订单
     */
    OmsOrder createOrder(Long memberId, OrderCreateDTO dto);

    /**
     * 使用余额支付订单：校验并扣减用户余额，成功后将订单置为待接单。
     */
    void pay(Long memberId, String orderSn);

    /**
     * 支付成功回调：将订单置为待接单并触发自动派单。
     * 若 memberId 非空则校验订单归属；供支付网关回调时传 null 跳过校验。
     */
    void confirmPaySuccess(Long memberId, String orderSn);

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
