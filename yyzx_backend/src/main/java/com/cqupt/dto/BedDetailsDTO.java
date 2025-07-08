package com.cqupt.dto;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/23 14:21
 * @description
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "BedDetailsDTO-床位管理查询条件", description = "")
public class BedDetailsDTO {
    @ApiModelProperty(value = "页码")
    private Integer pageSize;
    @ApiModelProperty(value = "客户姓名")
    private String customerName;
    @ApiModelProperty(value = "床位是否生效  0：生效  1：失效")
    private Integer isDeleted;
    @ApiModelProperty(value = "开始时间")
    private Date startDate;
    @ApiModelProperty(value = "结束时间")
    private Date endDate;
}
