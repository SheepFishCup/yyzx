package com.cqupt.dto;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/23 14:56
 * @description
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "KhxxDTO-客户信息查询条件", description = "")
public class KhxxDTO {
    @ApiModelProperty(value = "客户编号")
    private Long customerId;

    @ApiModelProperty(value = "客户姓名")
    private String customerName;

    @ApiModelProperty(value = "老人的类型 1-自理老人 2-护理老人 3-无管家")
    private Integer manType;

    @ApiModelProperty(value = "系统健康管家(护工)")
    private Long userId;

    @ApiModelProperty(value = "页码")
    private Integer pageSize;
}
