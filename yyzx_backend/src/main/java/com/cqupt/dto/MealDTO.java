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
@ApiModel(value = "MealDTO-套餐查询条件", description = "")
public class MealDTO {
    @ApiModelProperty(value = "页码")
    private Integer pageSize;
    @ApiModelProperty(value = "食谱编号")
    private Integer mealId;
    @ApiModelProperty(value = "星期")
    private String weekDay;
    @ApiModelProperty(value = "餐饮类型（早餐/午餐/晚餐）")
    private Integer mealType;
}
