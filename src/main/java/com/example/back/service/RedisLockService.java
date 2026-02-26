package com.example.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/**
 * 基于 Redis 的简单分布式锁服务。
 * 使用 SETNX + 过期时间获取锁，Lua 脚本安全释放锁。
 */
@Service
@RequiredArgsConstructor
public class RedisLockService {

    private static final String LOCK_KEY_PREFIX = "lock:";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 尝试获取分布式锁。
     *
     * @param key 锁业务键（不含前缀）
     * @param ttl 锁超时时间
     * @return 成功则返回锁标识 token，失败返回 null
     */
    public String tryLock(String key, Duration ttl) {
        String realKey = LOCK_KEY_PREFIX + key;
        String token = UUID.randomUUID().toString();
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(realKey, token, ttl);
        return Boolean.TRUE.equals(success) ? token : null;
    }

    /**
     * 释放分布式锁（仅持有者可释放）。
     *
     * @param key   锁业务键（不含前缀）
     * @param token 获取锁时返回的 token
     */
    public void unlock(String key, String token) {
        if (key == null || token == null) {
            return;
        }
        String realKey = LOCK_KEY_PREFIX + key;
        String lua = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end
                """;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(lua);
        script.setResultType(Long.class);
        stringRedisTemplate.execute(script, Collections.singletonList(realKey), token);
    }
}

