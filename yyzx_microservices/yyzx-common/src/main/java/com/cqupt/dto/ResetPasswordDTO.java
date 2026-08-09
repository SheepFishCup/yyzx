package com.cqupt.dto;
/*
 * Project: secendbook_backend
 * @author yyr
 * @date 2026/03/15 20:12
 * @description
 */

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class ResetPasswordDTO {
    @NotBlank(message = "令牌不能为空")
    private String token;

    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,20}$",
            message = "密码必须是6-20位字母数字组合")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
