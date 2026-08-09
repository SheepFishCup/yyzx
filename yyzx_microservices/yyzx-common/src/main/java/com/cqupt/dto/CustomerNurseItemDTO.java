package com.cqupt.dto;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/23 15:55
 * @description
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CustomerNurseItemDTO-客户护理项目查询条件", description = "")
public class CustomerNurseItemDTO {
    @ApiModelProperty(value = "客户编号")
    private Long customerId;
    @ApiModelProperty(value = "当前页", required = true, example = "1")
    private Integer current;
    @ApiModelProperty(value = "每页大小", required = true, example = "10")
    private Integer pageSize;
}
