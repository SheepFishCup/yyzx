package com.cqupt.dto;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/23 14:13
 * @description
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "UserDTO-用户查询的条件", description = "")
public class UserDTO {
    @ApiModelProperty(value = "当前页", required = true, example = "1")
    private Integer current;
    @ApiModelProperty(value = "每页大小", required = true, example = "6")
    private Integer pageSize;
    @ApiModelProperty(value = "系统角色编号（1-管理员，2-健康管家）")
    private Integer roleId;
    @ApiModelProperty(value = "用户真实姓名")
    private String nickName;
}
