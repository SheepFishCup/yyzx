package com.cqupt.controller.admin;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/25 17:19
 * @description
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.dto.MealDTO;
import com.cqupt.pojo.Meal;
import com.cqupt.service.MealService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.MealVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
//@RequestMapping("/admin/meal")
@RequestMapping("/meal")
@Api(tags = "膳食日历管理")
@CrossOrigin // 解决跨域问题
public class MealController {
    @Autowired
    MealService mealService;
    @GetMapping("/listMealPage")
    @ApiOperation("膳食查询（分页）/可以根据星期查询，根据膳食类型（早餐/午餐/晚餐）")
    public ResultVo<Page<MealVo>> listMealPage(MealDTO mealDTO) throws Exception{
        log.info("膳食查询,参数为：{}",mealDTO);
        return mealService.listMealVoPage(mealDTO);
    }

    @PostMapping("/addMeal")
    @ApiOperation("添加膳食")
    public ResultVo addMeal(Meal meal) throws Exception {
        log.info("添加膳食，参数为：{}",meal);
        mealService.save(meal);
        return ResultVo.ok("添加成功");
    }

    @PostMapping("/updateMeal")
    @ApiOperation("更新膳食")
    public ResultVo updateMeal(Meal meal) throws Exception {
        log.info("更新膳食，参数为：{}",meal);
        mealService.updateById(meal);
        return ResultVo.ok("更新成功");
    }

    @GetMapping("/removeMeal")
    @ApiOperation("删除膳食")
    public ResultVo removeMeal(Integer id) throws Exception {
        log.info("删除膳食，参数为：{}",id);
        mealService.removeById(id);
        return ResultVo.ok("删除成功");
    }

}
