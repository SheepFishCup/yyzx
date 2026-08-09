package com.cqupt.nursing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 护理服务 — 护理项目CRUD、护理等级管理、护理记录
 */
@SpringBootApplication(scanBasePackages = {"com.cqupt"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.cqupt.feign")
@EnableCaching
@MapperScan("com.cqupt.nursing.mapper")
public class YyzxNursingApplication {

    public static void main(String[] args) {
        SpringApplication.run(YyzxNursingApplication.class, args);
    }
}
