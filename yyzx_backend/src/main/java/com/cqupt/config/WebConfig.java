package com.cqupt.config;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/19 11:00
 * @description web配置
 */

import com.cqupt.interceptor.CheckTokenInterceptor;
import com.cqupt.utils.CustomDateDeserializer;
import com.cqupt.utils.DateConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;

import java.util.Date;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private CheckTokenInterceptor checkTokenInterceptor;
    //登录拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(checkTokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login",
                        "/swagger-resources/**", "/doc.html", "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-ui/index.html",
                        "/images/**");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 日期转换
        registry.addConverter(new DateConverter());
    }
    //静态资源映射
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/yyzx/images/**")
                .addResourceLocations("file:D:/BaiduNetdiskDownload/软件项目/yyzx/yyzx_backend/src/main/resources/static/images/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
