package com.cqupt.vo;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/20 22:57
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
@ApiModel(value = "CustomerStatsVO", description = "客户入住统计视图对象")
public class CustomerStatsVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总床位数")
    private Integer totalBeds;

    @ApiModelProperty(value = "已入住人数")
    private Integer occupiedBeds;

    @ApiModelProperty(value = "空闲床位数")
    private Integer availableBeds;

    @ApiModelProperty(value = "床位使用率（%）")
    private BigDecimal occupancyRate;

    @ApiModelProperty(value = "本月新入住人数")
    private Integer newCustomers;

    @ApiModelProperty(value = "本月退住人数")
    private Integer leftCustomers;

    @ApiModelProperty(value = "护理级别分布")
    private NursingLevelDistVo levelDistribution;
}
