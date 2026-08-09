package com.cqupt.checkinout.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.dto.BackdownDTO;
import com.cqupt.valid.groups.Add;
import com.cqupt.feign.BedFeignClient;
import com.cqupt.feign.CustomerFeignClient;
import com.cqupt.pojo.Backdown;
import com.cqupt.pojo.Bed;
import com.cqupt.pojo.Customer;
import com.cqupt.checkinout.service.BackdownService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.BackdownVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

/**
 * 退住管理控制器（Feign 版）
 * <p>审批退住时通过 OpenFeign 调用床位/客户服务，替代共享数据库直连</p>
 */
@Slf4j
@RestController
@RequestMapping("/backdown")
@Api(tags = "退住管理")
@CrossOrigin
public class BackdownController {

    @Autowired
    private BackdownService backdownService;

    /** Feign 客户端：替代原来的 BedService 跨模块调用 */
    @Autowired
    private BedFeignClient bedFeignClient;

    /** Feign 客户端：替代原来的 CustomerService 跨模块调用 */
    @Autowired
    private CustomerFeignClient customerFeignClient;

    @PostMapping("/listBackdownVo")
    @ApiOperation("查询退住详情")
    public ResultVo<Page<BackdownVo>> listBackdownVo(@Valid BackdownDTO backdownDTO) throws Exception {
        log.info("查询退住详情,参数为：{}", backdownDTO);
        return backdownService.listBackdownVo(backdownDTO);
    }

    @PostMapping("/examineBackdown")
    @ApiOperation("审批退住")
    public ResultVo<?> examineBackdown(Backdown backdown) throws Exception {
        log.info("审批退住,参数为：{}", backdown);
        Backdown bd = backdownService.getById(backdown.getId());

        // 审批通过 → 通过 Feign 释放床位（替代原来的 Mapper 直连）
        if (backdown.getAuditStatus() == 1) {
            ResultVo<Customer> customerResult = customerFeignClient.getById(bd.getCustomerId());
            if (customerResult.getData() != null) {
                Integer bedId = customerResult.getData().getBedId();
                if (bedId != null) {
                    bedFeignClient.updateStatus(bedId, 1); // 1=空闲
                    log.info("Feign 调用: 释放床位 bedId={}", bedId);
                }
            }
        }
        return backdownService.examineBackdown(backdown);
    }

    @DeleteMapping
    @ApiOperation("删除退住")
    public ResultVo<?> delBackdown(@RequestParam @NotEmpty(message = "id不能为空") Long id) throws Exception {
        log.info("删除退住,参数为：{}", id);
        return backdownService.delBackdown(id);
    }

    @PostMapping("/addBackdown")
    @ApiOperation("添加退住")
    public ResultVo<?> addBackdown(@RequestBody @Validated(Add.class) Backdown backdown) throws Exception {
        log.info("添加退住,参数为：{}", backdown);
        backdownService.save(backdown);
        return ResultVo.ok("添加退住成功");
    }
}
