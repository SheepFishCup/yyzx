package com.cqupt.report;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 报表服务 — 客户入住统计、财务报表导出
 */
@SpringBootApplication(scanBasePackages = {"com.cqupt"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.cqupt.feign")
@MapperScan("com.cqupt.report.mapper")
public class YyzxReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(YyzxReportApplication.class, args);
    }
}
