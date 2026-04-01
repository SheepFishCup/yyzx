package com.cqupt.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("邮件消息 DTO")
public class MailMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String to;
    
    private String subject;
    
    private String content;
    
    private Boolean isHtml;
}
