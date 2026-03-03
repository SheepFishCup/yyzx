package com.cqupt.pojo;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/21 15:53
 * @description
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "Outward对象", description = "Outward实体对象")
public class Outward implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "备注")
    private String remarks;

    @ApiModelProperty(value = "逻辑删除标记（0：显示；1：隐藏）")
    private Integer isDeleted;

    @ApiModelProperty(value = "客户ID")
    private Long customerId;

    @ApiModelProperty(value = "外出事由")
    @TableField(value = "outgoingreason")
    private String outgoingReason;

    @ApiModelProperty(value = "外出时间")
    @TableField(value = "outgoingtime")
    private Date outgoingTime;

    @ApiModelProperty(value = "预计回院时间")
    @TableField(value = "expectedreturntime")
    private Date expectedReturnTime;

    @ApiModelProperty(value = "实际回院时间")
    @TableField(value = "actualreturntime")
    private Date actualReturnTime;

    @ApiModelProperty(value = "陪同人")
    private String escorted;

    @ApiModelProperty(value = "与老人关系")
    private String relation;

    @ApiModelProperty(value = "陪同人电话")
    @TableField(value = "escortedtel")
    private String escortedTel;

    @ApiModelProperty(value = "审批状态  0-已提交 1-同意  2-拒绝")
    @TableField(value = "auditstatus")
    private Integer auditStatus;

    @ApiModelProperty(value = "审批人")
    @TableField(value = "auditperson")
    private String auditPerson;

    @ApiModelProperty(value = "审批时间")
    @TableField(value = "audittime")
    private Date auditTime;

    // User

}
