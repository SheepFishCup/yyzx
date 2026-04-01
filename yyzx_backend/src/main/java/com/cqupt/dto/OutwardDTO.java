package com.cqupt.dto;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/23 16:46
 * @description
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "OutwardDTO-退床条件", description = "")
public class OutwardDTO {
    @ApiModelProperty(value = "当前页", required = true, example = "1")
    private Integer current;

    @ApiModelProperty(value = "每页大小", required = true, example = "10")
    private Integer pageSize;

    @ApiModelProperty(value = "客户 ID")
    private Long customerId;

    @ApiModelProperty(value = "用户编号")
    private Long userId;
}
