package com.cqupt.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/25 16:54
 * @description
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.dto.MealDTO;
import com.cqupt.mapper.MealMapper;
import com.cqupt.pojo.Meal;
import com.cqupt.service.MealService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.MealVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MealServiceImpl extends ServiceImpl<MealMapper, Meal> implements MealService {

    @Autowired
    private MealMapper mealMapper;
    @Override
    public ResultVo<Page<MealVo>> listMealVoPage(MealDTO mealDTO) throws Exception {
        Page<MealVo> page=new Page<>(mealDTO.getPageSize(),5);
        mealMapper.selectMealVo(page,mealDTO.getWeekDay(),mealDTO.getMealType());
        return ResultVo.ok(page);
    }
}
