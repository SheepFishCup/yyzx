package com.cqupt.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/21 16:48
 * @description
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CustomerPreference对象", description = "CustomerPreference实体对象")
@TableName("customerpreference")
public class CustomerPreference implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "顾客ID")
    private Integer customerId;

    @ApiModelProperty(value = "饮食喜好")
    private String preferences;

    @ApiModelProperty(value = "注意事项")
    private String attention;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "逻辑删除标记（0：显示；1：隐藏）")
    private Integer isDeleted;
}
