package com.cqupt.feign;

import com.cqupt.pojo.BedDetails;
import com.cqupt.utils.ResultVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 床位详情 Feign 客户端
 * <p>供 yyzx-customer 等模块调用 yyzx-bed 的 BedDetails 操作</p>
 */
@FeignClient(name = "yyzx-bed", contextId = "bedDetailsFeignClient", path = "/yyzx/internal/beddetails",
        fallbackFactory = BedDetailsFeignClientFallbackFactory.class)
public interface BedDetailsFeignClient {

    /** 创建床位详情记录（客户入住时） */
    @PostMapping
    ResultVo<BedDetails> createBedDetails(@RequestBody BedDetails bedDetails);

    /** 关闭床位详情（客户退住/换床时） */
    @PutMapping("/close")
    ResultVo<Void> closeBedDetails(@RequestParam("bedDetailsId") Long bedDetailsId);

    /** 更新床位详情（如修改到期日） */
    @PutMapping("/{id}")
    ResultVo<Void> updateBedDetails(@PathVariable("id") Long id,
                                     @RequestBody BedDetails bedDetails);

    /** 按客户 ID 更新床位详情（退住标记删除 / 编辑修改到期日） */
    @PutMapping("/byCustomer/{customerId}")
    ResultVo<Void> updateByCustomer(@PathVariable("customerId") Long customerId,
                                     @RequestBody BedDetails bedDetails);
}
