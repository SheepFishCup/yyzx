package com.cqupt.controller;
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
import com.cqupt.service.NurseRecordService;
import com.cqupt.service.OutwardService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.NurseRecordsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nurserecord")
@Api(tags = "护理记录管理")
@CrossOrigin // 解决跨域问题
public class NurseRecordsController {
    @Autowired
    private NurseRecordService nurseRecordService;
    @Autowired
    private OutwardService outwardService;

    @PostMapping("/addNurseRecord")
    @ApiOperation("添加护理记录")
    public ResultVo addNurseRecord(NurseRecord nurseRecord) throws Exception{
        return nurseRecordService.addNurseRecord(nurseRecord);
    }

    @GetMapping("/listNurseRecordsVo")
    @ApiOperation("动态查询护理记录-分页")
    public ResultVo<Page<NurseRecordsVo>> listNurseRecordsVo(NurseRecordDTO nurseRecordDTO) throws Exception{
        return nurseRecordService.queryNurseRecordsVo(nurseRecordDTO);
    }

    @GetMapping("/removeCustomerRecord")
    @ApiOperation("删除护理记录")
    public ResultVo removeCustomerRecord(Integer id) throws Exception{
        return nurseRecordService.removeCustomerRecord(id);
    }

    @GetMapping("/queryOutwardVo")
    @ApiOperation("查询外出记录")
    public ResultVo queryOutwardVo(OutwardDTO outwardDTO) throws Exception{
        return outwardService.queryOutwardVo(outwardDTO);
    }
}
