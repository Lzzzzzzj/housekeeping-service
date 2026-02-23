package com.example.back.service;

/**
 * 师傅端 - 订单相关服务
 */
public interface StaffOrderService {

    /**
     * 服务人员接单/抢单
     *
     * @param staffUserId 当前登录服务人员对应的 sys_user.id
     * @param orderId     要接的订单ID
     */
    void grabOrder(Long staffUserId, Long orderId);
}

