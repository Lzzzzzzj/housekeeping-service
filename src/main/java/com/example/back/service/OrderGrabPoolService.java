package com.example.back.service;

import java.util.List;

/**
 * 待接单池（抢单池）基于 Redis 的缓冲层。
 */
public interface OrderGrabPoolService {

    /**
     * 订单进入待接单状态时加入抢单池。
     *
     * @param orderId 订单ID
     * @param score   用于排序的分数（通常使用创建时间或预约时间的时间戳）
     */
    void addToPool(Long orderId, double score);

    /**
     * 从抢单池中移除订单（如被接单或取消）。
     */
    void removeFromPool(Long orderId);

    /**
     * 拉取当前抢单池中的部分订单ID，按 score 升序。
     *
     * @param limit 最大数量
     */
    List<Long> listPoolIds(int limit);
}

