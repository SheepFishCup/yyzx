package com.cqupt.checkinout.config;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/21 08:20
 * @description
 */

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableKnife4j
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("《颐养中心》后端接口文档说明")
                        .description("文档说明，这是《颐养中心》后端接口文档")
                        .version("v2.1.1")
                        .contact(new Contact()
                                .name("yyr")
                                .email("2855733759@qq.com")));
    }
}
