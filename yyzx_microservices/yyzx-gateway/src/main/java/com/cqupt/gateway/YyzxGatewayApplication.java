package com.cqupt.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 颐养中心 API 网关
 * <p>
 * 功能：
 * <ul>
 *   <li>统一入口：前端所有请求经网关路由到对应微服务</li>
 *   <li>JWT 认证：在网关层完成 token 校验，下游服务无需重复验证</li>
 *   <li>负载均衡：通过 Nacos + Spring Cloud LoadBalancer 实现</li>
 *   <li>CORS 跨域：统一处理前端跨域请求</li>
 * </ul>
 */
@SpringBootApplication
@EnableDiscoveryClient
public class YyzxGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(YyzxGatewayApplication.class, args);
    }
}
