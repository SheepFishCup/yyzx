package com.cqupt.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("邮件消息 DTO")
public class NotifyMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    
    private String type;
    
    private String title;
    
    private String content;
    
    private Map<String, Object> extraData;
}
