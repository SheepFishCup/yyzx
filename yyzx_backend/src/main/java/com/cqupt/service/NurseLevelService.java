package com.cqupt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqupt.pojo.NurseLevel;
import com.cqupt.pojo.NurseLevelItem;
import com.cqupt.utils.ResultVo;

public interface NurseLevelService extends IService<NurseLevel> {
    ResultVo removeNurseLevelItem(Integer levelId, Integer itemId) throws Exception;

    ResultVo addItemToLevel(NurseLevelItem nurseLevelItem) throws Exception;
}
