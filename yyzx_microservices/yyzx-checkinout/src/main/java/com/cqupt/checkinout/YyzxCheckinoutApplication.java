package com.cqupt.checkinout;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 出入管理服务 — 退住审批、外出审批
 * <p>通过 OpenFeign 调用床位/客户服务，替代共享数据库直连</p>
 */
@SpringBootApplication(scanBasePackages = {"com.cqupt"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.cqupt.feign")
@MapperScan("com.cqupt.checkinout.mapper")
public class YyzxCheckinoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(YyzxCheckinoutApplication.class, args);
    }
}
