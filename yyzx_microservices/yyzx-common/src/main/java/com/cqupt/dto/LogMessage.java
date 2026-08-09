package com.cqupt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 日志消息 DTO
public class LogMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String level;
    
    private String module;
    
    private String action;
    
    private String message;
    
    private String operator;
    
    private LocalDateTime timestamp;
}
