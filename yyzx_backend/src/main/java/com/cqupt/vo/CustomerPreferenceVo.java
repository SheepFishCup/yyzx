package com.cqupt.vo;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/23 10:04
 * @description
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CustomerPreferenceVo", description = "")
public class CustomerPreferenceVo {
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

    //Customer
    @ApiModelProperty(value = "客户姓名")
    private String customerName;

    @ApiModelProperty(value = "客户年龄")
    private Integer customerAge;

    @ApiModelProperty(value = "客户性别  0：男  1：女")
    private Integer customerSex;
}
