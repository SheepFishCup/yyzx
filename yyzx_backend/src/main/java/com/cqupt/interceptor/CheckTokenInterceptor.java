package com.cqupt.interceptor;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/19 14:29
 * @description 拦截器
 */

import com.cqupt.constant.JwtClaimsConstant;
import com.cqupt.context.BaseContext;
import com.cqupt.exception.BusinessException;
import com.cqupt.properties.JwtProperties;
import com.cqupt.utils.JwtUtil;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class CheckTokenInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtProperties jwtProperties;

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
            BaseContext.setCurrentId(empId);  // 保存当前登录的员工id到threadLocal中
            log.info("当前员工id：{}", empId);
            //3、通过，放行
            return true;
        } catch (ExpiredJwtException e) {
            throw new BusinessException("token已过期");
        } catch (Exception e){
            throw new BusinessException("token不合法");
        }
    }
}
