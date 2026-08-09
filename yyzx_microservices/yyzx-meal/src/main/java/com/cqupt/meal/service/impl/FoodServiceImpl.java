package com.cqupt.meal.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/26 09:00
 * @description
 */

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.meal.mapper.FoodMapper;
import com.cqupt.pojo.Food;
import com.cqupt.meal.service.FoodService;
import org.springframework.stereotype.Service;

@Service
public class FoodServiceImpl extends ServiceImpl<FoodMapper, Food> implements FoodService {

}
