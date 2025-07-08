package com.cqupt.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/30 10:01
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.dto.OutwardDTO;
import com.cqupt.mapper.OutwardMapper;
import com.cqupt.pojo.Outward;
import com.cqupt.service.OutwardService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.OutwardVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutwardServiceImpl extends ServiceImpl<OutwardMapper, Outward> implements OutwardService {
    @Autowired
    private OutwardMapper outwardMapper;

    @Override
    public ResultVo examineOutward(Outward outward) throws Exception {
        UpdateWrapper<Outward> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",outward.getId());
        updateWrapper.set("auditstatus",outward.getAuditStatus());
        updateWrapper.set("audittime",outward.getAuditTime());
        updateWrapper.set("auditperson",outward.getAuditPerson());
        outwardMapper.update(outward,updateWrapper);
        return ResultVo.ok("审批成功");
    }

    @Override
    public ResultVo<Page<OutwardVo>> queryOutwardVo(OutwardDTO outwardDTO) throws Exception {
        Page<OutwardVo> page = new Page<>(outwardDTO.getPageSize(),6);
        outwardMapper.selectOutwardVo(page,outwardDTO.getUserId());
        return ResultVo.ok(page);
    }

    @Override
    public ResultVo delOutward(Integer id) throws Exception {
        UpdateWrapper<Outward> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        updateWrapper.set("is_deleted",1);
        outwardMapper.update(null,updateWrapper);
        return ResultVo.ok("删除成功");
    }

    @Override
    public ResultVo updateOutward(Outward outward) throws Exception {
        UpdateWrapper<Outward> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",outward.getId());
        int row = outwardMapper.updateById(outward);
        if (row<=0){
            return ResultVo.fail("更新失败");
        }
        return ResultVo.ok("更新成功");
    }

    @Override
    public ResultVo addOutward(Outward outward) throws Exception {
        int row = outwardMapper.insert(outward);
        if (row<=0){
            return ResultVo.fail("添加失败");
        }
        return ResultVo.ok("添加成功");
    }


}
