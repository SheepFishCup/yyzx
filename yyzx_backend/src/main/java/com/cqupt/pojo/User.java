package com.cqupt.pojo;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/21 09:09
 * @description
 */


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false) //自动生成Equals和HashCode
@ApiModel(value = "User对象", description = "User实体对象")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time")
    private Date createTime;

    @ApiModelProperty(value = "创建者")
    private Integer createBy;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "更新者")
    private Integer updateBy;

    @ApiModelProperty(value = "逻辑删除标记(0:显示，1:隐藏)")
    private Integer isDeleted;

    @ApiModelProperty(value = "真实姓名")
    private String nickname;

    @ApiModelProperty(value = "系统账号")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "性别(0女性，1男姓)")
    private Integer sex;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "手机号")
    private String phoneNumber;

    @ApiModelProperty(value = "系统角色编号(1-管理员，2-健康管家)")
    private Integer roleId;

    @ApiModelProperty(value = "当前角色的菜单")
    @TableField(exist = false)
    private List<Menu> menuList;

}
