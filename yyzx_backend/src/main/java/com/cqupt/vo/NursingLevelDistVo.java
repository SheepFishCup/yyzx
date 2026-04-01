package com.cqupt.vo;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/20 22:58
 * @description
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 护理级别分布 VO
 */
@Data
@Builder
@ApiModel(value = "NursingLevelDistVO", description = "护理级别分布视图对象")
public class NursingLevelDistVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "一级护理人数")
    private Integer levelOneCare;

    @ApiModelProperty(value = "二级护理人数")
    private Integer levelTwoCare;

    @ApiModelProperty(value = "三级护理人数")
    private Integer levelThreeCare;

    @ApiModelProperty(value = "四级护理人数")
    private Integer levelFourCare;

    @ApiModelProperty(value = "五级护理人数")
    private Integer levelFiveCare;

    @ApiModelProperty(value = "六级护理人数")
    private Integer levelSixCare;

    @ApiModelProperty(value = "七级护理人数")
    private Integer levelSevenCare;

    @ApiModelProperty(value = "八级护理人数")
    private Integer levelEightCare;

    @ApiModelProperty(value = "自理人数")
    private Integer selfCare;

}
