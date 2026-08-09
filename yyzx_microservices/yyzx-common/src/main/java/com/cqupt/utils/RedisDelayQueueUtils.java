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

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    public void addDelayTask(String queueName, String task, long delaySeconds) {
        long score = System.currentTimeMillis() + delaySeconds * 1000;
        redisTemplate.opsForZSet().add(queueName, task, score);
    }

    @SuppressWarnings("unchecked")
    public List<String> getDueTasks(String queueName, long limit) {
        long now = System.currentTimeMillis();

        Set<byte[]> byteObjects = redisTemplate.execute(connection -> {
            return connection.zRangeByScore(
                    queueName.getBytes(),
                    Double.MIN_VALUE,
                    (double) now,
                    0,
                    limit
            );
        }, true);

        List<String> tasks = new ArrayList<>();
        if (byteObjects != null) {
            for (byte[] bytes : byteObjects) {
                tasks.add(new String(bytes));
            }
        }
        return tasks;
    }

    public void removeTask(String queueName, String task) {
        redisTemplate.opsForZSet().remove(queueName, task);
    }

    public Long getPendingCount(String queueName) {
        long now = System.currentTimeMillis();
        return redisTemplate.execute(connection ->
                        connection.zCount(queueName.getBytes(), 0, (double) now)
                , true);    }

    @SuppressWarnings("unchecked")
    public long getNextTaskDelay(String queueName) {
        Set<org.springframework.data.redis.connection.RedisZSetCommands.Tuple> tuples = redisTemplate.execute(connection ->
                        connection.zRangeWithScores(queueName.getBytes(), 0, 0)
                , true);

        if (tuples == null || tuples.isEmpty()) {
            return -1;
        }

        org.springframework.data.redis.connection.RedisZSetCommands.Tuple first = tuples.iterator().next();
        if (first != null && first.getScore() != null) {
            return first.getScore().longValue() - System.currentTimeMillis();
        }
        return -1;
    }
}
