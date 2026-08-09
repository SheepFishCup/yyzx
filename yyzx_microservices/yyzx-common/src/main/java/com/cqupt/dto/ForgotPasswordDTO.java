package com.cqupt.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/*
 * Project: secendbook_backend
 * @author yyr
 * @date 2026/03/10 16:36
 * @description
 */
@Data
public class ForgotPasswordDTO {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "验证码不能为空")
    private String captcha;

    @NotBlank(message = "UUID不能为空")
    private String uuid;
}
