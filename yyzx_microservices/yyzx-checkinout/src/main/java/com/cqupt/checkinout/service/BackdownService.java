package com.cqupt.checkinout.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqupt.dto.BackdownDTO;
import com.cqupt.pojo.Backdown;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.BackdownVo;

public interface BackdownService extends IService<Backdown> {
    ResultVo<Page<BackdownVo>> listBackdownVo(BackdownDTO backdownDTO) throws Exception;

    ResultVo examineBackdown(Backdown backdown) throws Exception;

    ResultVo delBackdown(Long id) throws Exception;
}
