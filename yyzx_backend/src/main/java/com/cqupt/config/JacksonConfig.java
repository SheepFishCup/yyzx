package com.cqupt.config;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/07/06 11:04
 * @description
 */

import com.cqupt.utils.CustomDateDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

@Configuration
public class JacksonConfig {

    @Autowired
    private CustomDateDeserializer dateDeserializer; // 你的自定义反序列化器

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(Date.class, dateDeserializer);
            builder.modules(module);
        };
    }
}
