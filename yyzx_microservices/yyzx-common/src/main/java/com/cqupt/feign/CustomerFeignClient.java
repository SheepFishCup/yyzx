package com.cqupt.feign;

import com.cqupt.pojo.Customer;
import com.cqupt.utils.ResultVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 客户服务 Feign 客户端
 * <p>供 checkinout、bed、task 等模块调用</p>
 */
@FeignClient(name = "yyzx-customer", contextId = "customerFeignClient", path = "/yyzx/internal/customer",
        fallbackFactory = CustomerFeignClientFallbackFactory.class)
public interface CustomerFeignClient {

    /** 根据 ID 获取客户 */
    @GetMapping("/{customerId}")
    ResultVo<Customer> getById(@PathVariable("customerId") Long customerId);

    /** 更新客户护理等级 */
    @PutMapping("/{customerId}/level")
    ResultVo<Void> updateLevel(@PathVariable("customerId") Long customerId,
                                @RequestParam("levelId") Integer levelId);

    /** 通用客户更新（如换床时修改 bedId） */
    @PutMapping("/{customerId}")
    ResultVo<Void> updateCustomer(@PathVariable("customerId") Long customerId,
                                   @RequestBody Customer customer);
}
