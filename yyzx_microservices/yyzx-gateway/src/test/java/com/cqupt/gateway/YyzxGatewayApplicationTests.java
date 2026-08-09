package com.cqupt.gateway;

import com.cqupt.gateway.filter.AuthProperties;
import com.cqupt.gateway.filter.JwtAuthGlobalFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 网关启动与上下文加载测试
 */
@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false",
        "spring.redis.host=localhost",
        "spring.redis.port=6379"
})
@ActiveProfiles("test")
class YyzxGatewayApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private AuthProperties authProperties;

    @Autowired
    private JwtAuthGlobalFilter jwtAuthGlobalFilter;

    /**
     * 测试 Spring 容器正常启动
     */
    @Test
    void contextLoads() {
        assertNotNull(context);
        assertTrue(context.containsBean("yyzxGatewayApplication"));
    }

    /**
     * 测试 JWT 过滤器已注册
     */
    @Test
    void jwtFilterRegistered() {
        assertNotNull(jwtAuthGlobalFilter);
    }

    /**
     * 测试认证配置已加载
     */
    @Test
    void authPropertiesLoaded() {
        assertNotNull(authProperties);
        assertEquals("cqupt123456", authProperties.getJwtSecret());
        assertFalse(authProperties.getExcludePaths().isEmpty());
        // 验证白名单包含登录相关路径
        assertTrue(authProperties.getExcludePaths().stream()
                .anyMatch(p -> p.contains("loginWithCaptcha")));
        assertTrue(authProperties.getExcludePaths().stream()
                .anyMatch(p -> p.contains("doc.html")));
    }

    /**
     * 测试白名单路径匹配
     */
    @Test
    void excludePathMatching() {
        // 使用 AntPathMatcher 验证白名单
        org.springframework.util.AntPathMatcher matcher = new org.springframework.util.AntPathMatcher();

        // 精确匹配
        assertFalse(authProperties.getExcludePaths().stream()
                .anyMatch(p -> matcher.match(p, "/admin/generate")));
        // 实际上应该在白名单中
        assertTrue(authProperties.getExcludePaths().stream()
                .anyMatch(p -> "/admin/generate".equals(p) || matcher.match(p, "/admin/generate")));
    }
}
