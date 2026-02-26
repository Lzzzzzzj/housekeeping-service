package com.example.back.service.impl;

import com.example.back.service.OrderGrabPoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 待接单池实现，使用 Redis ZSet 存储订单ID。
 */
@Service
@RequiredArgsConstructor
public class OrderGrabPoolServiceImpl implements OrderGrabPoolService {

    private static final String KEY_GRAB_POOL = "order:grab_pool";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void addToPool(Long orderId, double score) {
        if (orderId == null) return;
        stringRedisTemplate.opsForZSet()
                .add(KEY_GRAB_POOL, orderId.toString(), score);
    }

    @Override
    public void removeFromPool(Long orderId) {
        if (orderId == null) return;
        stringRedisTemplate.opsForZSet()
                .remove(KEY_GRAB_POOL, orderId.toString());
    }

    @Override
    public List<Long> listPoolIds(int limit) {
        if (limit <= 0) {
            limit = 50;
        }
        var ids = stringRedisTemplate.opsForZSet()
                .range(KEY_GRAB_POOL, 0, limit - 1);
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return ids.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}

