package com.cqupt.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 认证授权服务 — 登录、验证码、用户管理、角色菜单管理
 */
@SpringBootApplication(scanBasePackages = {"com.cqupt"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.cqupt.feign")
@EnableAsync
@EnableCaching
@MapperScan("com.cqupt.auth.mapper")
public class YyzxAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(YyzxAuthApplication.class, args);
    }
}
