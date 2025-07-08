package com.cqupt.controller;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/28 10:17
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqupt.pojo.NurseLevelItem;
import com.cqupt.service.NurseLevelItemService;
import com.cqupt.utils.ResultVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nurselevelitem")
@Api(tags = "护理级别项目管理")
@CrossOrigin // 解决跨域问题
public class NurseLevelItemController {
    @Autowired
    private NurseLevelItemService nurseLevelItemService;

    @GetMapping("/listNurseLevelItem")
    public ResultVo listNurseLevelItem(NurseLevelItem nurseLevelItem) throws Exception {
        QueryWrapper qw=new QueryWrapper();
        if (nurseLevelItem.getLevelId()!=null){
            qw.eq("id",nurseLevelItem.getId());
        }
        return ResultVo.ok(nurseLevelItemService.list(qw));
    }

    @PostMapping("/addNurseLevelItem")
    public ResultVo addNurseLevelItem(NurseLevelItem nurseLevelItem) throws Exception {
        return ResultVo.ok(nurseLevelItemService.save(nurseLevelItem));
    }

    @PostMapping("/updateNurseLevelItem")
    public ResultVo updateNurseLevelItem(NurseLevelItem nurseLevelItem) throws Exception {
        return ResultVo.ok(nurseLevelItemService.updateById(nurseLevelItem));
    }

    @PostMapping("/removeNurseLevelItem")
    public ResultVo removeNurseLevelItem(Integer levelId,Integer itemId) throws Exception {
        QueryWrapper qw=new QueryWrapper();
        qw.eq("level_id",levelId);
        qw.eq("item_id",itemId);
        nurseLevelItemService.remove(qw);
        return ResultVo.ok("删除成功");
    }
}
