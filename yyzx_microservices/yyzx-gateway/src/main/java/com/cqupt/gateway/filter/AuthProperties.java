package com.cqupt.gateway.filter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * JWT 认证白名单配置
 * <p>白名单路径直接放行，不校验 token</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "yyzx.gateway")
public class AuthProperties {

    /** JWT 签名密钥 */
    private String jwtSecret = "cqupt123456";

    /** 不需要认证的路径（支持 Ant 风格通配符，含 /yyzx 前缀确保 Gateway 透传匹配） */
    private List<String> excludePaths = Arrays.asList(
            "/admin/generate",           "/yyzx/admin/generate",
            "/admin/loginWithCaptcha",   "/yyzx/admin/loginWithCaptcha",
            "/admin/login",              "/yyzx/admin/login",
            "/admin/forgotPassword",     "/yyzx/admin/forgotPassword",
            "/admin/resetPassword",      "/yyzx/admin/resetPassword",
            "/admin/verifyResetToken",   "/yyzx/admin/verifyResetToken",
            "/patient/login",            "/yyzx/patient/login",
            "/doc.html",                 "/yyzx/doc.html",
            "/webjars/**",               "/yyzx/webjars/**",
            "/v3/api-docs/**",           "/yyzx/v3/api-docs/**",
            "/swagger-resources/**",     "/yyzx/swagger-resources/**",
            "/swagger-ui.html",          "/yyzx/swagger-ui.html",
            "/swagger-ui/**",            "/yyzx/swagger-ui/**",
            "/images/**",                "/yyzx/images/**",
            "/druid/**",                 "/yyzx/druid/**"
    );
}
