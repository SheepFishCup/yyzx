package com.cqupt.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Component
public class RedisDelayQueueUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void addDelayTask(String queueName, String task, long delaySeconds) {
        long score = System.currentTimeMillis() + delaySeconds * 1000;
        redisTemplate.opsForZSet().add(queueName, task, score);
    }

    @SuppressWarnings("unchecked")
    public List<String> getDueTasks(String queueName, long limit) {
        long now = System.currentTimeMillis();
        Set<Object> objects = redisTemplate.opsForZSet()
                .rangeByScore(queueName, 0, now, 0, limit);

        List<String> tasks = new ArrayList<>();
        if (objects != null) {
            for (Object obj : objects) {
                if (obj instanceof String) {
                    tasks.add((String) obj);
                }
            }
        }
        return tasks;
    }

    public void removeTask(String queueName, String task) {
        redisTemplate.opsForZSet().remove(queueName, task);
    }

    public Long getPendingCount(String queueName) {
        long now = System.currentTimeMillis();
        return redisTemplate.opsForZSet().count(queueName, 0, now);
    }

    @SuppressWarnings("unchecked")
    public long getNextTaskDelay(String queueName) {
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                .rangeWithScores(queueName, 0, 0);

        if (tuples == null || tuples.isEmpty()) {
            return -1;
        }

        ZSetOperations.TypedTuple<Object> first = tuples.iterator().next();
        if (first != null && first.getScore() != null) {
            return first.getScore().longValue() - System.currentTimeMillis();
        }
        return -1;
    }
}
