package com.cqupt.nursing.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/28 10:25
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.nursing.mapper.NurseLevelItemMapper;
import com.cqupt.nursing.mapper.NurseLevelMapper;
import com.cqupt.pojo.NurseLevel;
import com.cqupt.pojo.NurseLevelItem;
import com.cqupt.nursing.service.NurseLevelService;
import com.cqupt.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NurseLevelServiceImpl extends ServiceImpl<NurseLevelMapper, NurseLevel> implements NurseLevelService {
    @Autowired
    NurseLevelMapper nurseLevelMapper;
    @Autowired
    private NurseLevelItemMapper nurseLevelItemMapper;

    @Override
    public ResultVo removeNurseLevelItem(Integer levelId, Integer itemId){
        QueryWrapper qw=new QueryWrapper();
        qw.eq("level_id",levelId);
        qw.eq("item_id",itemId);
        nurseLevelItemMapper.delete(qw);
        return ResultVo.ok("删除成功");
    }

    @Override
    public ResultVo addItemToLevel(NurseLevelItem nurseLevelItem) throws Exception {
        QueryWrapper qw=new QueryWrapper();
        qw.eq("level_id",nurseLevelItem.getLevelId());
        qw.eq("item_id",nurseLevelItem.getItemId());
        Long row=nurseLevelItemMapper.selectCount(qw);
        if (row>0){
            return ResultVo.fail("该护理项目已添加");
        }
        nurseLevelItemMapper.insert(nurseLevelItem);
        return ResultVo.ok("添加成功");
    }
}
