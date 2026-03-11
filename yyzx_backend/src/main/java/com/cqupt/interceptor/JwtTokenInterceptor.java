package com.cqupt.interceptor;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/19 14:29
 * @description 拦截器
 */

import com.cqupt.constant.JwtClaimsConstant;
import com.cqupt.constant.RedisConstant;
import com.cqupt.context.BaseContext;
import com.cqupt.exception.BusinessException;
import com.cqupt.properties.JwtProperties;
import com.cqupt.utils.JwtUtil;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }
        //放行预检请求，如果是options请求则直接放行，不进行拦截
        if (request.getMethod().equalsIgnoreCase("options")) {
            return true;
        }
        //swagger的静态资源也不用拦截
        if (request.getRequestURI().startsWith("/swagger") ||
                request.getRequestURI().startsWith("/v3/api-docs") ||
                request.getRequestURI().startsWith("/webjars") ||
                request.getRequestURI().equals("/doc.html")) {
            return true;
        }
        //图片的静态资源也不用拦截
        if (request.getRequestURI().toString().contains("images")) {
            return true;
        }
        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getUserHeader());
        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecret(), token);//解析令牌
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());//获取员工id
            // 3、从 Redis 验证 token 是否存在（支持强制下线）
            String tokenKey = RedisConstant.USER_TOKEN_PREFIX + empId;
            String cachedToken = (String) redisTemplate.opsForValue().get(tokenKey);

            if (cachedToken == null) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"登录已过期，请重新登录\"}");
                throw new BusinessException("登录已过期，请重新登录");
            }

            if (!cachedToken.equals(token)) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"token 不匹配，可能已在其他地方登录\"}");
                throw new BusinessException("token 不匹配");
            }

            // 4、刷新 token 过期时间（续期）
            redisTemplate.expire(tokenKey, RedisConstant.TOKEN_EXPIRE_SECONDS, java.util.concurrent.TimeUnit.SECONDS);

            BaseContext.setCurrentId(empId);  // 保存当前登录的员工id到threadLocal中
            log.info("当前员工id：{}", empId);
            //3、通过，放行
            return true;
        } catch (ExpiredJwtException e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"token 已过期\"}");
            throw new BusinessException("token已过期");
        } catch (Exception e){
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"token 不合法\"}");
            throw new BusinessException("token不合法");
        }
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("清除线程: {}中的threadLocal变量", Thread.currentThread().getId());
        BaseContext.removeCurrentId(); // 清除避免内存溢出
    }
}
