package com.cqupt.controller;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/26 09:01
 * @description
 */

import com.cqupt.pojo.Food;
import com.cqupt.pojo.Room;
import com.cqupt.service.FoodService;
import com.cqupt.utils.ResultVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/food")
@Api(tags = "食物管理") // swagger分组
@CrossOrigin
public class FoodContoller {
    @Autowired
    private FoodService foodService;

//    @GetMapping("/listFood")
//    @ApiOperation("食物列表查询")
//    public ResultVo<List<Food>> listFood() {
//        return ResultVo.ok(foodService.list());
//    }
    @GetMapping("/listFood")
    @ApiOperation("食物列表查询")
    public List<Food> listFood() {
        return foodService.list();
    }

}
