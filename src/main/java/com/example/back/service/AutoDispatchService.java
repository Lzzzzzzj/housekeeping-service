package com.example.back.service;

/**
 * 自动派单服务：根据权重公式为待接单订单分配合适的师傅（开启自动接单且听单中）。
 * 触发时机：支付成功回调、定时任务兜底。
 */
public interface AutoDispatchService {

    /**
     * 尝试为指定订单自动分配师傅。
     * 若未开启自动派单、无候选师傅或抢单失败则不分配。
     *
     * @param orderId 订单ID
     */
    void tryAutoAssign(Long orderId);
}
