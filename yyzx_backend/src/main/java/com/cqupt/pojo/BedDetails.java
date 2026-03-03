package com.cqupt.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/21 15:07
 * @description
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "BedDetails对象", description = "BedDetails实体对象")
@TableName("beddetails")
public class BedDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "床位起始日期")
    private Date startDate;

    @ApiModelProperty(value = "床位结束日期")
    private Date endDate;

    @ApiModelProperty(value = "床位详情信息")
    private String bedDetails;

    @ApiModelProperty(value = "顾客ID")
    private Long customerId;

    @ApiModelProperty(value = "床位ID")
    private Integer bedId;

    @ApiModelProperty(value = "逻辑删除标记（0：显示；1：隐藏）")
    private Integer isDeleted;
}
