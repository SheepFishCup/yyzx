package com.cqupt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.dto.NurseItemDTO;
import com.cqupt.pojo.NurseContent;
import com.cqupt.service.NurseContentService;
import com.cqupt.utils.ResultVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/28 10:19
 * @description
 */

@Slf4j
@RestController
//@RequestMapping("/admin/nursecontent")
@RequestMapping("/nursecontent")
@Api(tags = "护理内容管理")
@CrossOrigin // 解决跨域问题
public class NurseContentController {
    @Autowired
    private NurseContentService nurseContentService;

    @GetMapping("/listNurseItemByLevel")
    @ApiOperation("查询护理项目列表-按护理等级查询")
    public ResultVo<List<NurseContent>> listNurseItemByLevel(Integer levelId) throws Exception {
        log.info("查询护理项目列表-按护理等级查询,参数为：{}",levelId);
        return nurseContentService.listNurseItemByLevel(levelId);
    }

    @PostMapping("/addNurseItem")
    @ApiOperation("添加护理项目")
    public ResultVo addNurseItem(NurseContent nurseContent) throws Exception {
        log.info("添加护理项目，参数为：{}",nurseContent);
        nurseContentService.save(nurseContent);
        return ResultVo.ok("添加护理项目");
    }

    @PostMapping("/updateNurseItem")
    @ApiOperation("修改护理项目")
    public ResultVo updateNurseItem(NurseContent nursCcontent) throws Exception {
        log.info("修改护理项目，参数为：{}",nursCcontent);
        return nurseContentService.updateNurseItem(nursCcontent);
    }

    @GetMapping("/delNurseItem")
    @ApiOperation("删除护理项目")
    public ResultVo delNurseItem(@RequestParam @NotEmpty(message = "id不能为空") Integer id) throws Exception {
        log.info("删除护理项目，参数为：{}",id);
        return nurseContentService.delNurseItem(id);
    }

    @GetMapping("/findNurseItemPage")
    @ApiOperation("查询护理项目-分页")
    public ResultVo<Page<NurseContent>> findNurseItemPage(NurseItemDTO nurseItemDTO) throws Exception {
        log.info("查询护理项目-分页，参数为：{}",nurseItemDTO);
        Page<NurseContent> page=new Page<>(nurseItemDTO.getPageSize(),6);
        QueryWrapper qw=new QueryWrapper();
        if(nurseItemDTO.getItemName()!=null && nurseItemDTO.getItemName()!=""){
            qw.like("nursing_name","%"+nurseItemDTO.getItemName()+"%");
        }
        qw.eq("status",nurseItemDTO.getStatus());
        qw.eq("is_deleted",0); //显示
        nurseContentService.page(page,qw);
        return ResultVo.ok(page);
    }
}
