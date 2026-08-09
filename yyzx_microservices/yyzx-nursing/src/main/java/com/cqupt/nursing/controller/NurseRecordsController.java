package com.cqupt.nursing.controller;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/30 09:55
 * @description
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.dto.NurseRecordDTO;
import com.cqupt.dto.OutwardDTO;
import com.cqupt.pojo.NurseRecord;
import com.cqupt.feign.BackdownFeignClient;
import com.cqupt.nursing.service.NurseRecordService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.NurseRecordsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 护理记录管理（Feign 版）
 * <p>外出记录查询改为通过 Feign 调用 checkinout 服务</p>
 */
@Slf4j
@RestController
@RequestMapping("/nurserecord")
@Api(tags = "护理记录管理")
@CrossOrigin
public class NurseRecordsController {
    @Autowired
    private NurseRecordService nurseRecordService;

    @Autowired
    private BackdownFeignClient backdownFeignClient;

    @PostMapping("/addNurseRecord")
    @ApiOperation("添加护理记录")
    public ResultVo addNurseRecord(NurseRecord nurseRecord) throws Exception{
        log.info("添加护理记录{}", nurseRecord);
        return nurseRecordService.addNurseRecord(nurseRecord);
    }

    @GetMapping("/listNurseRecordsVo")
    @ApiOperation("动态查询护理记录-分页")
    public ResultVo<Page<NurseRecordsVo>> listNurseRecordsVo(NurseRecordDTO nurseRecordDTO) throws Exception{
        log.info("动态查询护理记录{}", nurseRecordDTO);
        return nurseRecordService.queryNurseRecordsVo(nurseRecordDTO);
    }

    @GetMapping("/removeCustomerRecord")
    @ApiOperation("删除护理记录")
    public ResultVo removeCustomerRecord(Long id) throws Exception{
        log.info("删除护理记录,参数为:{}", id);
        return nurseRecordService.removeCustomerRecord(id);
    }

    @GetMapping("/queryOutwardVo")
    @ApiOperation("查询外出记录")
    public ResultVo<?> queryOutwardVo(OutwardDTO outwardDTO) {
        log.info("查询外出记录（Feign → checkinout）,参数为:{}", outwardDTO);
        return backdownFeignClient.queryOutwardVo(
                outwardDTO.getCurrent() != null ? outwardDTO.getCurrent() : 1L,
                outwardDTO.getPageSize() != null ? outwardDTO.getPageSize() : 10L,
                outwardDTO.getUserId(),
                outwardDTO.getCustomerId());
    }
}
