package com.cqupt.dto;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/10 12:07
 * @description
 */

import lombok.Data;

@Data
public class ImageCodeDTO {
    private String uuid;        // 验证码唯一标识
    private String base64;      // 图片base64数据
    private String code;        // 验证码文本（仅开发测试用）
}