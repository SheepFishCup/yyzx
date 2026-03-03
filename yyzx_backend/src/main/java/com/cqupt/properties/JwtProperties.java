package com.cqupt.properties;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/02/28 22:44
 * @description
 */

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "yyzx.jwt")
@Data
public class JwtProperties {
    // 管理员密钥
    private String userSecret;
    private long userExpire;
    private String userHeader;
}
