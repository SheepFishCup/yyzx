package com.cqupt.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/30 14:20
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.dto.BackdownDTO;
import com.cqupt.mapper.BackdownMapper;
import com.cqupt.pojo.Backdown;
import com.cqupt.service.BackdownService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.BackdownVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class BackdownServiceImpl extends ServiceImpl<BackdownMapper, Backdown> implements BackdownService {
    @Autowired
    private BackdownMapper backdownMapper;

    @Override
    public ResultVo<Page<BackdownVo>> listBackdownVo(BackdownDTO backdownDTO) throws Exception {
        Page<BackdownVo> page = new Page<>(backdownDTO.getPageSize(),6);
        backdownMapper.selectBackdownVo(page,backdownDTO.getUserId());
        return ResultVo.ok(page);
    }

    //审批退住申请
    @Override
    public ResultVo examineBackdown(Backdown backdown) throws Exception {
        UpdateWrapper<Backdown> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("id",backdown.getId());
        updateWrapper.set("auditstatus",backdown.getAuditStatus());
        updateWrapper.set("audittime",backdown.getAuditTime());
        updateWrapper.set("auditperson",backdown.getAuditPerson());
        backdownMapper.update(backdown,updateWrapper);
        return ResultVo.ok("审批成功");
    }


    @Override
    public ResultVo delBackdown(Integer id) throws Exception {
        UpdateWrapper<Backdown> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        updateWrapper.set("is_deleted",1);
        backdownMapper.update(null,updateWrapper);
        return ResultVo.ok("撤回成功");
    }
}
