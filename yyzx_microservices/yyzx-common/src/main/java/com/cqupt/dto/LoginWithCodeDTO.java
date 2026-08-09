package com.cqupt.dto;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/10 12:08
 * @description
 */

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginWithCodeDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String captcha;    // 用户输入的验证码

    @NotBlank(message = "验证码UUID不能为空")
    private String uuid;         // 验证码对应的UUID
}
