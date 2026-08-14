package com.cqupt.checkinout.controller;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/30 17:13
 * @description
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.dto.OutwardDTO;
import com.cqupt.feign.BedFeignClient;
import com.cqupt.feign.CustomerFeignClient;
import com.cqupt.pojo.Outward;
import com.cqupt.checkinout.service.OutwardService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.OutwardVo;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.seata.spring.annotation.GlobalTransactional;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 外出管理控制器（Feign 版）
 * <p>审批外出时通过 OpenFeign 调用床位/客户服务</p>
 */
@Slf4j
@RestController
@RequestMapping("/outward")
@Api(tags = "外出管理")
@CrossOrigin
public class OutwardController {
    @Autowired
    private OutwardService outwardService;

    @Autowired
    private BedFeignClient bedFeignClient;

    @Autowired
    private CustomerFeignClient customerFeignClient;
    @GetMapping("/queryOutwardVo")
    @ApiOperation("查询外出详情")
    public ResultVo<Page<OutwardVo>> queryOutwardVo(OutwardDTO outwardDTO) throws Exception {
        log.info("查询外出详情{}", outwardDTO);
        return outwardService.queryOutwardVo(outwardDTO);
    }
    @GlobalTransactional(name = "checkinout-examine-outward", rollbackFor = Exception.class)
    @PostMapping("/examineOutward")
    @ApiOperation("审批外出")
    public ResultVo<?> examineOutward(Outward outward) throws Exception {
        log.info("审批外出{}", outward);
        Outward ow = outwardService.getById(outward.getId());
        // 审批通过 → 通过 Feign 更新床位状态为外出（3）
        if (ow.getAuditStatus() == 1) {
            ResultVo<com.cqupt.pojo.Customer> customerResult =
                    customerFeignClient.getById(ow.getCustomerId());
            if (customerResult.getData() != null) {
                Integer bedId = customerResult.getData().getBedId();
                if (bedId != null) {
                    bedFeignClient.updateStatus(bedId, 3); // 3=外出
                    log.info("Feign 调用: 设置床位 {} 为外出状态", bedId);
                }
            }
        }
        return outwardService.examineOutward(outward);
    }
    @GetMapping("/delOutward")
    @ApiOperation("删除退住")
    public ResultVo delOutward(Long id) throws Exception {
        log.info("删除外出{}", id);
        return outwardService.delOutward(id);
    }
    @PostMapping("/updateOutward")
    @ApiOperation("修改退住")
    public ResultVo updateOutward(Outward outward) throws Exception {
        log.info("修改外出{}", outward);
        return outwardService.updateOutward(outward);
    }
    @PostMapping("/addOutward")
    @ApiOperation("添加退住")
    public ResultVo addOutward(Outward outward) throws Exception {
        log.info("添加外出{}", outward);
        outwardService.save(outward);
//        outwardService.addOutward(outward);
        return ResultVo.ok("添加成功");
    }

    @PostMapping("/updateBackTime")
    @ApiOperation("登记回院时间")
    public ResultVo updateBackTime(Outward outward) throws Exception {
        log.info("登记回院时间{}", outward);
        UpdateWrapper<Outward> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",outward.getId());
        outwardService.updateById(outward);
        return ResultVo.ok("登记时间成功");
    }

}
