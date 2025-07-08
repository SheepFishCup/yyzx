package com.cqupt.config;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/19 11:00
 * @description web配置
 */

import com.cqupt.utils.CustomDateDeserializer;
import com.cqupt.utils.DateConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Date;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    /**
     * 配置拦截器链
     * @param registry 拦截器注册器，用于添加或移除拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebMvcConfigurer.super.addInterceptors(registry);
    }
    /**
     * 添加日期格式转换器
     * @param registry 格式化注册器，用于注册类型转换器
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new DateConverter());
    }

    /**
     * 配置静态资源映射
     * @param registry 资源处理器注册器，用于注册资源路径和实际存储位置
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/yyzx/images/**")
                .addResourceLocations("file:D:/BaiduNetdiskDownload/2025实训/yyzx_backend/src/main/resources/static/images/");
    }

}
