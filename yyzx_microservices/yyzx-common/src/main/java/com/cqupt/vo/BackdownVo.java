package com.cqupt.vo;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/23 09:51
 * @description
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "BackdownVo", description = "")
public class BackdownVo {

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "备注")
    private String remark;

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

    //BedDetails
    @ApiModelProperty(value = "床位详情信息")
    private String bedDetails;

    @ApiModelProperty(value = "床位ID")
    private Integer bedId;

    //Customer
    @ApiModelProperty(value = "客户姓名")
    private String customerName;

    @ApiModelProperty(value = "入住时间")
    private Date checkinDate;
}
