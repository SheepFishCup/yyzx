package com.cqupt.vo;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/23 09:26
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
@ApiModel(value = "OutwardVo", description = "")
public class OutwardVo {

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "逻辑删除标记（0：显示；1：隐藏）")
    private Integer isDeleted;

    @ApiModelProperty(value = "客户ID")
    private Integer customerId;

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

    //User
    @ApiModelProperty(value = "真实姓名")
    private String nickName;

    @ApiModelProperty(value = "手机号")
    private String phoneNumber;

    //NurseContent
    @ApiModelProperty(value = "护理项目编号")
    private String serialNumber;

    @ApiModelProperty(value = "护理项目名称")
    private String nursingName;

    //Customer
    @ApiModelProperty(value = "客户姓名")
    private String customerName;
}
