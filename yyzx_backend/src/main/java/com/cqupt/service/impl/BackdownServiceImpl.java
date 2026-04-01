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
import com.cqupt.context.BaseContext;
import com.cqupt.dto.BackdownDTO;
import com.cqupt.mapper.BackdownMapper;
import com.cqupt.pojo.Backdown;
import com.cqupt.service.BackdownService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.BackdownVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class BackdownServiceImpl extends ServiceImpl<BackdownMapper, Backdown> implements BackdownService {
    @Autowired
    private BackdownMapper backdownMapper;

    @Override
    public ResultVo<Page<BackdownVo>> listBackdownVo(BackdownDTO backdownDTO) throws Exception {
        Integer current = backdownDTO.getCurrent() != null ? backdownDTO.getCurrent() : 1;
        Integer pageSize = backdownDTO.getPageSize() != null ? backdownDTO.getPageSize() : 10;

        // 参数范围校验，防止恶意请求
        if (current < 1) {
            current = 1;  // 最小为第 1 页
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;  // 超出范围使用默认值
        }

        Page<BackdownVo> page = new Page<>(current, pageSize);
        backdownMapper.selectBackdownVo(page, backdownDTO.getUserId(), backdownDTO.getCustomerId());
        return ResultVo.ok(page);
    }

    //审批退住申请
    @Override
    public ResultVo examineBackdown(Backdown backdown) throws Exception {
        UpdateWrapper<Backdown> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("id",backdown.getId());
        updateWrapper.set("auditstatus",backdown.getAuditStatus());
        updateWrapper.set("audittime", LocalDateTime.now());
        updateWrapper.set("auditperson", BaseContext.getCurrentId());
        backdownMapper.update(backdown,updateWrapper);
        return ResultVo.ok("审批成功");
    }

    @Override
    public ResultVo delBackdown(Long id) throws Exception {
        UpdateWrapper<Backdown> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        updateWrapper.set("is_deleted",1);
        backdownMapper.update(null,updateWrapper);
        return ResultVo.ok("撤回成功");
    }
}
