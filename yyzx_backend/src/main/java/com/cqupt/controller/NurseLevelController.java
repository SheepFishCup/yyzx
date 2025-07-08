package com.cqupt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqupt.pojo.NurseContent;
import com.cqupt.pojo.NurseLevel;
import com.cqupt.pojo.NurseLevelItem;
import com.cqupt.service.NurseContentService;
import com.cqupt.service.NurseLevelItemService;
import com.cqupt.service.NurseLevelService;
import com.cqupt.utils.ResultVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/28 10:25
 * @description
 */
@RestController
@RequestMapping("/nurselevel")
@Api(tags = "护理级别管理")
@CrossOrigin // 解决跨域问题
public class NurseLevelController {
    @Autowired
    private NurseLevelService nurseLevelService;
    @Autowired
    private NurseContentService nurseContentService;

    @PostMapping("/addNurseLevel")
    @ApiOperation("添加护理级别")
    public ResultVo addNurseLevel(NurseLevel nurseLevel) throws Exception {
        nurseLevelService.save(nurseLevel);
        return ResultVo.ok("添加护理级别");
    }

    @PostMapping("/updateNurseLevel")
    @ApiOperation("修改护理级别")
    public ResultVo updateNurseLevel(NurseLevel nurseLevel) throws Exception {
        nurseLevelService.updateById(nurseLevel);
        return ResultVo.ok("修改护理级别");
    }

    @GetMapping("/removeNurseLevel")
    @ApiOperation("删除护理级别")
    public ResultVo removeNurseLevel(Integer id) throws Exception {
        nurseLevelService.removeById(id);
        return ResultVo.ok("删除护理级别");
    }
    @GetMapping("/listNurseLevel")
    @ApiOperation("查询护理级别")
    public ResultVo listNurseLevel(NurseLevel nurseLevel) throws Exception {
        QueryWrapper qw=new QueryWrapper();
        if (nurseLevel.getLevelStatus()!=null){
            qw.eq("level_status",nurseLevel.getLevelStatus());
        }
        return ResultVo.ok(nurseLevelService.list(qw));
    }

    @GetMapping("/listNurseItemByLevel")
    @ApiOperation("查询护理项目-不分页")
    public ResultVo<List<NurseContent>> listNurseItemByLevel(Integer levelId) throws Exception {
        return nurseContentService.listNurseItemByLevel(levelId);
    }

    @PostMapping("/addItemToLevel")
    @ApiOperation("添加护理项目到护理级别")
    public ResultVo addItemToLevel(NurseLevelItem nurseLevelItem) throws Exception {
//        QueryWrapper qw=new QueryWrapper();
//        qw.eq("level_id",nurseLevelItem.getLevelId());
//        qw.eq("item_id",nurseLevelItem.getItemId());
//        int row=nurseLevelItemService.count(qw);
//        if (row>0){
//            return ResultVo.fail("该护理项目已添加");
//        }
//        nurseLevelItemService.save(nurseLevelItem);
//        return ResultVo.ok("添加成功");
        return nurseLevelService.addItemToLevel(nurseLevelItem);
    }

    @GetMapping("/removeNurseLevelItem")
    @ApiOperation("删除护理级别中的护理项目")
    public ResultVo removeNurseLevelItem(Integer levelId,Integer itemId) throws Exception {
//        QueryWrapper qw=new QueryWrapper();
//        qw.eq("level_id",levelId);
//        qw.eq("item_id",itemId);
//        nurseLevelItemService.remove(qw);
//        return ResultVo.ok("删除成功");
        return nurseLevelService.removeNurseLevelItem(levelId,itemId);
    }

}
