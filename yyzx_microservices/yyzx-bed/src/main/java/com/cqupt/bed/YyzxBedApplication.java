package com.cqupt.bed;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 床位管理服务 — 床位CRUD、房间管理、床位交换
 */
@SpringBootApplication(scanBasePackages = {"com.cqupt"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.cqupt.feign")
@MapperScan("com.cqupt.bed.mapper")
public class YyzxBedApplication {

    public static void main(String[] args) {
        SpringApplication.run(YyzxBedApplication.class, args);
    }
}
