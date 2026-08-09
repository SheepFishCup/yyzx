package com.cqupt.nursing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqupt.dto.NurseItemDTO;
import com.cqupt.pojo.NurseContent;
import com.cqupt.utils.ResultVo;

import java.util.List;

public interface NurseContentService extends IService<NurseContent> {
    ResultVo<List<NurseContent>>  listNurseItemByLevel(Integer levelId) throws Exception;

    ResultVo updateNurseItem(NurseContent nursCcontent) throws Exception;

    ResultVo delNurseItem(Integer id) throws Exception;

}
