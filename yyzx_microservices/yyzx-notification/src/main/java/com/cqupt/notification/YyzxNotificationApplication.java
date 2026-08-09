package com.cqupt.notification;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 通知服务 — RabbitMQ消息消费、邮件发送、钉钉通知、WebSocket推送
 * 本服务不对外暴露REST接口，主要作为消息消费者运行
 */
@SpringBootApplication(scanBasePackages = {"com.cqupt"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.cqupt.feign")
@EnableAsync
@MapperScan("com.cqupt.notification.mapper")
public class YyzxNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(YyzxNotificationApplication.class, args);
    }
}
