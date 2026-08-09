package com.cqupt.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "dingtalk.robot")
public class DingTalkConfig {
    
    /**
     * 钉钉机器人 Webhook URL
     */
    private String webhook;
    
    /**
     * 加签密钥（安全设置 - 加签）
     */
    private String secret;
}
