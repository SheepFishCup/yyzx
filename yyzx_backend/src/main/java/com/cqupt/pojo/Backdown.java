package com.cqupt.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/21 16:11
 * @description
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "BackDown对象", description = "BackDown实体对象")
public class Backdown implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "备注")
    private String remarks;

    @ApiModelProperty(value = "逻辑删除标记（0：显示；1：隐藏）")
    private Integer isDeleted;

    @ApiModelProperty(value = "客户ID")
    private Integer customerId;

    @ApiModelProperty(value = "退住时间")
    @TableField(value = "retreattime")
    private Date retreatTime;

    @ApiModelProperty(value = "退住类型 0-正常退住  1-死亡退住 2-保留床位")
    @TableField(value = "retreattype")
    private Integer retreatType;

    @ApiModelProperty(value = "退住原因")
    @TableField(value = "retreatreason")
    private String retreatReason;

    @ApiModelProperty(value = "审批状态  0-已提交 1-同意  2-拒绝")
    @TableField(value = "auditstatus")
    private Integer auditStatus;

    @ApiModelProperty(value = "审批人")
    @TableField(value = "auditperson")
    private String auditPerson;

    @ApiModelProperty(value = "审批时间")
    @TableField(value = "audittime")
    private Date auditTime;

}
