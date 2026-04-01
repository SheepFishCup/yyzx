package com.cqupt.vo;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/20 23:03
 * @description
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@ApiModel(value = "FinanceStatsVO", description = "财务统计视图对象")
public class FinanceStatsVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总收入")
    private BigDecimal totalIncome;

    @ApiModelProperty(value = "住宿费收入")
    private BigDecimal accommodationIncome;

    @ApiModelProperty(value = "护理费收入")
    private BigDecimal nursingIncome;

    @ApiModelProperty(value = "餐饮费收入")
    private BigDecimal foodIncome;

    @ApiModelProperty(value = "其他收入")
    private BigDecimal otherIncome;

    @ApiModelProperty(value = "欠费总额")
    private BigDecimal arrearsTotal;

    @ApiModelProperty(value = "欠费客户数")
    private Integer arrearsCustomerCount;

    @ApiModelProperty(value = "环比增长率（%）")
    private BigDecimal growthRate;
}
