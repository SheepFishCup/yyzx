package com.cqupt.vo;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/23 09:00
 * @description
 */

import com.cqupt.pojo.Room;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "CwsyBedVo", description = "床位使用视图")
public class CwsyBedVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总床位")
    private Integer zcw;

    @ApiModelProperty(value = "空床位")
    private Integer kx;

    @ApiModelProperty(value = "有人床位")
    private Integer yr;

    @ApiModelProperty(value = "外出床位")
    private Integer wc;

    @ApiModelProperty(value = "床位和房间列表")
    private List<Room> roomList;

}
