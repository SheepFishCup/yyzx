package com.cqupt.bed.controller;

import com.cqupt.pojo.Bed;
import com.cqupt.pojo.BedDetails;
import com.cqupt.bed.mapper.BedMapper;
import com.cqupt.bed.mapper.BedDetailsMapper;
import com.cqupt.utils.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 床位服务内部 API（供其他微服务通过 OpenFeign 调用）
 * <p>路径 /internal/** 已在 JWT 拦截器中排除认证</p>
 */
@Slf4j
@RestController
public class BedInternalController {

    @Autowired
    private BedMapper bedMapper;

    @Autowired
    private BedDetailsMapper bedDetailsMapper;

    // ==================== 床位 ====================

    @GetMapping("/internal/bed/{bedId}")
    public ResultVo<Bed> getBedById(@PathVariable Integer bedId) {
        return ResultVo.ok(bedMapper.selectById(bedId));
    }

    @GetMapping("/internal/bed/checkAvailable")
    public ResultVo<Boolean> checkAvailable(@RequestParam Integer bedId) {
        Bed bed = bedMapper.selectById(bedId);
        return ResultVo.ok(bed != null && bed.getBedStatus() == 1);
    }

    @PutMapping("/internal/bed/updateStatus")
    public ResultVo<Void> updateStatus(@RequestParam Integer bedId,
                                        @RequestParam Integer status) {
        Bed bed = new Bed();
        bed.setId(bedId);
        bed.setBedStatus(status);
        bedMapper.updateById(bed);
        log.info("Feign: bed {} → status {}", bedId, status);
        return ResultVo.ok("操作成功");
    }

    // ==================== 床位详情 ====================

    @PostMapping("/internal/beddetails")
    public ResultVo<BedDetails> createBedDetails(@RequestBody BedDetails bedDetails) {
        bedDetailsMapper.insert(bedDetails);
        log.info("Feign: create BedDetails id={}, customerId={}, bedId={}",
                bedDetails.getId(), bedDetails.getCustomerId(), bedDetails.getBedId());
        return ResultVo.ok(bedDetails);
    }

    @PutMapping("/internal/beddetails/close")
    public ResultVo<Void> closeBedDetails(@RequestParam Long bedDetailsId) {
        BedDetails bd = new BedDetails();
        bd.setId(bedDetailsId);
        bd.setIsDeleted(1);
        bedDetailsMapper.updateById(bd);
        log.info("Feign: close BedDetails id={}", bedDetailsId);
        return ResultVo.ok("操作成功");
    }

    /** 更新床位详情（如修改到期日等） */
    @PutMapping("/internal/beddetails/{id}")
    public ResultVo<Void> updateBedDetails(@PathVariable Long id,
                                            @RequestBody BedDetails bedDetails) {
        bedDetails.setId(id);
        bedDetailsMapper.updateById(bedDetails);
        log.info("Feign: update BedDetails id={}", id);
        return ResultVo.ok("操作成功");
    }

    /** 按客户 ID 更新床位详情（退住时标记 is_deleted，编辑时修改 endDate） */
    @PutMapping("/internal/beddetails/byCustomer/{customerId}")
    public ResultVo<Void> updateBedDetailsByCustomer(@PathVariable Long customerId,
                                                      @RequestBody BedDetails bedDetails) {
        bedDetailsMapper.update(bedDetails,
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<BedDetails>()
                        .eq("customer_id", customerId)
                        .eq("is_deleted", 0));
        log.info("Feign: update BedDetails by customerId={}", customerId);
        return ResultVo.ok("操作成功");
    }
}
