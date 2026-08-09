package com.cqupt.auth.config;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/19 11:00
 * @description web配置
 */

import com.cqupt.interceptor.JwtTokenInterceptor;
import com.cqupt.interceptor.InternalAuthInterceptor;
import com.cqupt.utils.DateConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.*;


@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private JwtTokenInterceptor jwtTokenAdminInterceptor;
    //登录拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InternalAuthInterceptor())
                .addPathPatterns("/internal/**");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/patient/login",
                        "/admin/generate", "/admin/loginWithCaptcha", "/admin/login",
                        "/swagger-resources/**", "/doc.html", "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-ui/index.html",
                        "/images/**",
                        "/internal/**");
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
                .addResourceLocations("classpath:/static/images/");
    }
    //跨域
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
