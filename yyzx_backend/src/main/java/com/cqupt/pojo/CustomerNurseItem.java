package com.cqupt.pojo;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/21 15:27
 * @description
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CustomerNurseItem对象", description = "CustomerNurseItem实体对象")
@TableName("customernurseitem")
public class CustomerNurseItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "护理项目编号")
    private Integer itemId;

//    数据库字段为custormerId
    @ApiModelProperty(value = "客户编号")
    @TableField("custormer_id")
    private Integer customerId;

    @ApiModelProperty(value = "护理级别编号")
    private Integer levelId;

    @ApiModelProperty(value = "购买数量")
    private Integer nurseNumber;

    @ApiModelProperty(value = "逻辑删除标记（0：显示；1：隐藏）")
    private Integer isDeleted;

    @ApiModelProperty(value = "服务购买日期")
    private Date buyTime;

    @ApiModelProperty(value = "服务到期时间")
    private Date maturityTime;
}