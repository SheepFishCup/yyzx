package com.cqupt.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;


public class JwtUtil {
    // 生成token
    public static String createToken(String secretKey,long expire,Map<String, Object>claims) {
        Date nowDate = new Date();
        Date exp = new Date(nowDate.getTime() + expire);  // 设置过期时间
        return Jwts.builder()
                // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                .setClaims(claims)
                // 使用HS512签名算法和密钥签名
                .signWith(SignatureAlgorithm.HS512, secretKey.getBytes(StandardCharsets.UTF_8))
                .setExpiration(exp)    // 设置过期时间
                .compact();
    }

    // 解析token
    public static Claims parseJWT(String secretKey, String token) {
        // 得到DefaultJwtParser
        Claims claims = Jwts.parser()
                // 设置签名的秘钥
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                // 设置需要解析的jwt
                .parseClaimsJws(token).getBody();
        return claims;
    }

    // 验证token是否过期
    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

}
