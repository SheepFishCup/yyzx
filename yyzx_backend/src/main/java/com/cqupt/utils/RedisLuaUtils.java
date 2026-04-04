package com.cqupt.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RedisLuaUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 执行 Lua 脚本 - 返回 Long
     */
    public Long executeScriptLong(String script, List<String> keys, String... args) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);
        return redisTemplate.execute(redisScript, keys, args);
    }

    /**
     * 执行 Lua 脚本 - 返回 Boolean
     */
    public Boolean executeScriptBoolean(String script, List<String> keys, String... args) {
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Boolean.class);
        return redisTemplate.execute(redisScript, keys, args);
    }

    /**
     * 执行 Lua 脚本 - 返回 Object
     */
    public Object executeScript(String script, List<String> keys, String... args) {
        DefaultRedisScript<Object> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Object.class);
        return redisTemplate.execute(redisScript, keys, args);
    }

    /**
     * 分布式限流：滑动窗口算法
     * @param key 限流 key
     * @param maxCount 最大请求次数
     * @param windowSize 时间窗口（秒）
     * @return true-允许访问，false-拒绝访问
     */
    public boolean rateLimit(String key, long maxCount, long windowSize) {
        String script = 
            "local key = KEYS[1] " +
            "local maxCount = tonumber(ARGV[1]) " +
            "local windowSize = tonumber(ARGV[2]) " +
            "local now = redis.call('TIME') " +
            "local currentTimestamp = now[1] .. string.rep('0', 6) + now[2] " +
            "local windowStart = currentTimestamp - windowSize * 1000000 " +
            "redis.call('ZREMRANGEBYSCORE', key, '-inf', windowStart) " +
            "local count = redis.call('ZCARD', key) " +
            "if count < maxCount then " +
            "    redis.call('ZADD', key, currentTimestamp, currentTimestamp) " +
            "    redis.call('EXPIRE', key, windowSize) " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";
        
        Long result = executeScriptLong(script, Collections.singletonList(key), 
                String.valueOf(maxCount), String.valueOf(windowSize));
        return result != null && result == 1;
    }

    /**
     * 分布式锁 - 简单版本（推荐使用 Redisson）
     */
    public Boolean tryLock(String key, String requestId, long expireTime) {
        String script = 
            "if redis.call('SET', KEYS[1], ARGV[1], 'NX', 'PX', ARGV[2]) then " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";
        
        return executeScriptBoolean(script, Collections.singletonList(key), 
                requestId, String.valueOf(expireTime));
    }

    /**
     * 释放分布式锁
     */
    public Boolean unlock(String key, String requestId) {
        String script = 
            "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('DEL', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";
        
        return executeScriptBoolean(script, Collections.singletonList(key), requestId);
    }

    /**
     * 批量删除指定前缀的 Key
     */
    public Long deleteKeysByPattern(String pattern) {
        String script = 
            "local keys = redis.call('KEYS', ARGV[1]) " +
            "if #keys > 0 then " +
            "    return redis.call('DEL', unpack(keys)) " +
            "else " +
            "    return 0 " +
            "end";
        
        return executeScriptLong(script, Collections.emptyList(), pattern);
    }
}
