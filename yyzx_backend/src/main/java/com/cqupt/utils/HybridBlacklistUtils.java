package com.cqupt.utils;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class HybridBlacklistUtils {

    @Autowired
    private RedissonClient redissonClient;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private RBloomFilter<String> bloomFilter;
    
    private static final String BLACKLIST_SET_KEY = "yyzx:blacklist:usernames";
    private static final String BLOOM_FILTER_NAME = "yyzx:bloom:user:blacklist";

    @PostConstruct
    public void init() {
        bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_NAME);
        bloomFilter.tryInit(100000L, 0.01);
    }

    /**
     * 添加到黑名单
     */
    public void addToBlacklist(String username) {
        String key = "blacklist:username:" + username;
        bloomFilter.add(key);
        redisTemplate.opsForSet().add(BLACKLIST_SET_KEY, key);
    }

    /**
     * 检查是否在黑名单中
     * @return true-在黑名单中，false-不在黑名单中
     */
    public boolean isInBlacklist(String username) {
        String key = "blacklist:username:" + username;
        // 使用布隆过滤器检查
        if (!bloomFilter.contains(key)) {
            return false;
        }
        // 使用Redis Set检查
        Boolean isMember = redisTemplate.opsForSet().isMember(BLACKLIST_SET_KEY, key);
        return Boolean.TRUE.equals(isMember);
    }

    /**
     * 从黑名单中移除
     * @return true-移除成功，false-移除失败（不在黑名单中）
     */
    public boolean removeFromBlacklist(String username) {
        String key = "blacklist:username:" + username;
        Long removed = redisTemplate.opsForSet().remove(BLACKLIST_SET_KEY, key);
        return removed > 0;
    }

    /**
     * 获取黑名单用户数量
     */
    public long getBlacklistCount() {
        return redisTemplate.opsForSet().size(BLACKLIST_SET_KEY);
    }

    /**
     * 批量添加到黑名单
     */
    public void addBatchToBlacklist(String... usernames) {
        for (String username : usernames) {
            addToBlacklist(username);
        }
    }

    /**
     * 批量从黑名单中移除
     */
    public void removeBatchFromBlacklist(String... usernames) {
        for (String username : usernames) {
            removeFromBlacklist(username);
        }
    }
}
