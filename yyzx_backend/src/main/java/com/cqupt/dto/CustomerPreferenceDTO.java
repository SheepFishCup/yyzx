package com.cqupt.dto;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/23 15:56
 * @description
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CustomerPreferenceDTO-顾客喜好查询条件", description = "")
public class CustomerPreferenceDTO {
    @ApiModelProperty(value = "喜好编号")
    private Integer customerPreferenceId;
    @ApiModelProperty(value = "顾客姓名")
    private String customerName;
    @ApiModelProperty(value = "页码")
    private Integer pageSize;
}
