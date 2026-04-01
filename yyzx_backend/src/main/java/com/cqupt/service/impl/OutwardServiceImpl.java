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
        // 设置默认分页参数，避免 null
        Integer current = outwardDTO.getCurrent() != null ? outwardDTO.getCurrent() : 1;
        Integer pageSize = outwardDTO.getPageSize() != null ? outwardDTO.getPageSize() : 10;

        // 参数范围校验，防止恶意请求
        if (current < 1) {
            current = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        Page<OutwardVo> page = new Page<>(current, pageSize);
        outwardMapper.selectOutwardVo(page, outwardDTO.getUserId(), outwardDTO.getCustomerId());
        return ResultVo.ok(page);
    }

    @Override
    public ResultVo delOutward(Long id) throws Exception {
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
