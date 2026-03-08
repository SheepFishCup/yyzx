package com.cqupt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAsync // 开启异步
@EnableCaching // 开启缓存
@MapperScan("com.cqupt.mapper")
@EnableScheduling
public class YyzxBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(YyzxBackendApplication.class, args);

    }

}
