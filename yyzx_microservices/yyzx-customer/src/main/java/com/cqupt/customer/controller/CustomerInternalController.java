package com.cqupt.customer.controller;

import com.cqupt.pojo.Customer;
import com.cqupt.customer.mapper.CustomerMapper;
import com.cqupt.utils.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 客户服务内部 API（供其他微服务通过 OpenFeign 调用）
 */
@Slf4j
@RestController
@RequestMapping("/internal/customer")
public class CustomerInternalController {

    @Autowired
    private CustomerMapper customerMapper;

    @GetMapping("/{customerId}")
    public ResultVo<Customer> getById(@PathVariable Long customerId) {
        Customer customer = customerMapper.selectById(customerId);
        return ResultVo.ok(customer);
    }

    @PutMapping("/{customerId}/level")
    public ResultVo<Void> updateLevel(@PathVariable Long customerId,
                                       @RequestParam Integer levelId) {
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setLevelId(levelId);
        customerMapper.updateById(customer);
        log.info("Feign: 更新客户 {} 护理等级为 {}", customerId, levelId);
        return ResultVo.ok("操作成功");
    }

    /** 通用客户更新（如换床时更新 bedId） */
    @PutMapping("/{customerId}")
    public ResultVo<Void> updateCustomer(@PathVariable Long customerId,
                                          @RequestBody Customer customer) {
        customer.setId(customerId);
        customerMapper.updateById(customer);
        log.info("Feign: 更新客户 id={}", customerId);
        return ResultVo.ok("操作成功");
    }
}
