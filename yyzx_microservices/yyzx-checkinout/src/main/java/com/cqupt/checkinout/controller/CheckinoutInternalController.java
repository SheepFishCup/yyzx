package com.cqupt.checkinout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.dto.OutwardDTO;
import com.cqupt.pojo.Backdown;
import com.cqupt.checkinout.mapper.BackdownMapper;
import com.cqupt.checkinout.mapper.OutwardMapper;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.OutwardVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 出入管理内部 API（供 yyzx-task / yyzx-nursing 通过 OpenFeign 调用）
 */
@Slf4j
@RestController
@RequestMapping("/internal")
public class CheckinoutInternalController {

    @Autowired
    private BackdownMapper backdownMapper;

    @Autowired
    private OutwardMapper outwardMapper;

    // ==================== 退住 ====================

    @GetMapping("/backdown/pending")
    public ResultVo<List<Backdown>> listPending() {
        return ResultVo.ok(backdownMapper.selectList(
                new LambdaQueryWrapper<Backdown>()
                        .eq(Backdown::getAuditStatus, 0)
                        .eq(Backdown::getIsDeleted, 0)));
    }

    @PutMapping("/backdown/{id}/approve")
    public ResultVo<Void> approve(@PathVariable Long id) {
        Backdown bd = backdownMapper.selectById(id);
        if (bd != null) {
            bd.setAuditStatus(1);
            bd.setAuditPerson("0");
            bd.setAuditTime(new java.util.Date());
            bd.setRemarks("系统自动审批：超过24小时未人工处理");
            backdownMapper.updateById(bd);
            log.info("Feign: auto-approve backdown id={}", id);
        }
        return ResultVo.ok("操作成功");
    }

    // ==================== 外出 ====================

    /** 分页查询外出记录（供 nursing 的护理记录页面使用） */
    @GetMapping("/outward/queryVo")
    public ResultVo<Page<OutwardVo>> queryOutwardVo(OutwardDTO outwardDTO) throws Exception {
        Page<OutwardVo> page = new Page<>(outwardDTO.getCurrent(), outwardDTO.getPageSize());
        outwardMapper.selectOutwardVo(page,
                outwardDTO.getUserId(), outwardDTO.getCustomerId());
        return ResultVo.ok(page);
    }
}
