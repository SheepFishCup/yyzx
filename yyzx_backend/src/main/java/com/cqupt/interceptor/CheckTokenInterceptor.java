package com.cqupt.interceptor;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/19 14:29
 * @description 拦截器
 */

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class CheckTokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //放行预检请求，如果是options请求则直接放行，不进行拦截
        if (request.getMethod().equalsIgnoreCase("options")) {
            return true;
        }
        //图片的静态资源也不用拦截
        if (request.getRequestURI().toString().contains("images")) {
            return true;
        }
        //获取token
        String token = request.getHeader("token");
        if (token == null) {
            throw new RuntimeException("token为空,请先登录");
        } else {
            //验证token的有效性、时效性、正确性
            JwtParser parser = Jwts.parser();
            //解析token.这个token必须和key所生成的token一致
            parser.setSigningKey("cqupt123456");
            //验证token是否正确
            Jws<Claims> claimsJws = parser.parseClaimsJws(token);
        }
        return true;
    }
}
