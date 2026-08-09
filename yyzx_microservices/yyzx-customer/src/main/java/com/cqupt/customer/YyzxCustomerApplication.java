package com.cqupt.customer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 客户管理服务 — 客户CRUD、偏好管理、护理项目管理、小程序端
 */
@SpringBootApplication(scanBasePackages = {"com.cqupt"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.cqupt.feign")
@EnableAsync
@EnableCaching
@MapperScan("com.cqupt.customer.mapper")
public class YyzxCustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(YyzxCustomerApplication.class, args);
    }
}
