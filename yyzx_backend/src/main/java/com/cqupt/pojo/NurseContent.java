package com.cqupt.pojo;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/21 16:24
 * @description
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "NurseContent对象", description = "NurseContent实体对象")
@TableName("nursecontent")
public class NurseContent implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "编号")
    private String serialNumber;

    @ApiModelProperty(value = "名称")
    private String nursingName;

    @ApiModelProperty(value = "价格")
    private String servicePrice;

    @ApiModelProperty(value = "描述")
    private String message;

    @ApiModelProperty(value = "状态 1：启用；2：停用")
    private Integer status;

    @ApiModelProperty(value = "执行周期")
    private String executionCycle;

    @ApiModelProperty(value = "执行次数")
    private String executionTimes;

    @ApiModelProperty(value = "逻辑删除标记（0：显示；1：隐藏）")
    private Integer isDeleted;
}
