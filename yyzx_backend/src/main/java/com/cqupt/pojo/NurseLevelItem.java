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
 * @date 2025/06/21 14:57
 * @description
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "NurseLevelItem对象", description = "NurseLevelItem实体对象")
@TableName("nurselevelitem")
public class NurseLevelItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "关联护理级别")
    private Integer levelId;

    @ApiModelProperty(value = "关联护理项目")
    private Integer itemId;
}
