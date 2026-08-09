package com.cqupt.gateway.filter;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 全局认证过滤器（无状态版本）
 * <p>
 * 纯 JWT 签名校验，不依赖 Redis：
 * <ol>
 *   <li>白名单路径直接放行</li>
 *   <li>从 Header 中提取 token</li>
 *   <li>验证 JWT 签名 + 过期时间</li>
 *   <li>从 JWT claims 中提取用户信息（userId、username、roleId）</li>
 *   <li>通过 X-User-Id / X-User-Name / X-User-Role 头传给下游服务</li>
 * </ol>
 *
 * <h3>无状态设计说明</h3>
 * <ul>
 *   <li>不再检查 Redis 中 token 是否存在，完全信任 JWT 签名</li>
 *   <li>强制下线方案：修改 JWT 密钥使所有旧 token 失效（紧急）或缩短 token 有效期（常规）</li>
 *   <li>建议生产环境配合 refresh token 机制，access token 有效期设为 30 分钟</li>
 * </ul>
 */
@Slf4j
@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthGlobalFilter(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 1. 白名单直接放行
        if (isExcluded(path)) {
            return chain.filter(exchange);
        }

        // 2. 提取 token
        String token = exchange.getRequest().getHeaders().getFirst("token");
        if (token == null) {
            return unauthorized(exchange, "未提供认证 token");
        }

        // 3. 解析 JWT（纯签名校验，不依赖 Redis）
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return unauthorized(exchange, "token 已过期，请重新登录");
        } catch (SignatureException | MalformedJwtException e) {
            return unauthorized(exchange, "token 无效");
        } catch (Exception e) {
            log.error("JWT 解析异常", e);
            return unauthorized(exchange, "token 解析失败");
        }

        // 4. 提取 JWT claims 中的用户信息
        Object userIdObj = claims.get("empId");
        if (userIdObj == null) {
            userIdObj = claims.get("userId");
        }
        if (userIdObj == null) {
            return unauthorized(exchange, "token 中缺少用户标识");
        }
        String userId = String.valueOf(userIdObj);
        String username = String.valueOf(claims.getOrDefault("username", ""));
        String roleId = String.valueOf(claims.getOrDefault("roleId", ""));

        // 5. 将用户信息注入 Header 传给下游服务
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", userId)
                .header("X-User-Name", username)
                .header("X-User-Role", roleId)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isExcluded(String path) {
        return authProperties.getExcludePaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("flag", false);
        body.put("message", message);
        body.put("data", null);

        byte[] bytes = JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
