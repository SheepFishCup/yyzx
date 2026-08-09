package com.cqupt.task;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务服务 — Quartz 定时任务调度、Redis 延迟队列处理
 */
@SpringBootApplication(scanBasePackages = {"com.cqupt"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.cqupt.feign")
@EnableScheduling
@MapperScan("com.cqupt.task.mapper")
public class YyzxTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(YyzxTaskApplication.class, args);
    }
}
