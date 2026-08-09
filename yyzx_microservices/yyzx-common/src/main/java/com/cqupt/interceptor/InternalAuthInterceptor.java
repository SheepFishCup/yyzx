package com.cqupt.interceptor;

import com.cqupt.constant.InternalTokenConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 内部服务认证拦截器
 * <p>只拦截 /internal/** 路径，校验 X-Internal-Token 共享密钥</p>
 * <p>Feign 调用通过 FeignRequestInterceptor 自动携带此 Header</p>
 */
@Slf4j
public class InternalAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        String path = request.getRequestURI();

        // 只拦截 /internal/ 路径
        if (!path.contains("/internal/")) {
            return true;
        }

        String token = request.getHeader(InternalTokenConstant.HEADER_NAME);
        if (InternalTokenConstant.DEFAULT_TOKEN.equals(token)) {
            return true;
        }

        log.warn("内部API认证失败: path={}, token={}", path,
                token != null ? "provided" : "missing");
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(
                    "{\"flag\":false,\"message\":\"内部服务认证失败\",\"data\":null}");
        } catch (Exception ignored) {}
        return false;
    }
}
