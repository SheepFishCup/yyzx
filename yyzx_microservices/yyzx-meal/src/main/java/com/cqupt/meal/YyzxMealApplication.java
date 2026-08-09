package com.cqupt.meal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 膳食服务 — 菜品管理、每周膳食计划
 */
@SpringBootApplication(scanBasePackages = {"com.cqupt"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.cqupt.feign")
@EnableCaching
@MapperScan("com.cqupt.meal.mapper")
public class YyzxMealApplication {

    public static void main(String[] args) {
        SpringApplication.run(YyzxMealApplication.class, args);
    }
}
