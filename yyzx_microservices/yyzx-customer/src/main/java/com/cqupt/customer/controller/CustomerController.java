package com.cqupt.customer.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.dto.KhxxDTO;
import com.cqupt.pojo.Customer;
import com.cqupt.customer.service.CustomerService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.KhxxCustomerVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 客户管理控制器（Sentinel 增强版）
 * <p>核心接口 listKhxxPage 配置了 QPS 限流 + 熔断降级</p>
 */
@Slf4j
@RestController
@RequestMapping("/customer")
@Api(tags = "客户管理")
@CrossOrigin
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * 客户列表分页查询 — Sentinel 限流保护
     * <p>blockHandler: 超出 QPS 阈值时触发</p>
     * <p>fallback: 服务异常时触发降级</p>
     */
    @GetMapping("/listKhxxPage")
    @ApiOperation("客户信息列表查询(分页)")
    @SentinelResource(value = "listKhxxPage",
            blockHandler = "listKhxxPageBlockHandler",
            fallback = "listKhxxPageFallback")
    public ResultVo<Page<KhxxCustomerVo>> listKhxxPage(KhxxDTO khxxDTO) throws Exception {
        log.info("客户信息列表查询(分页),参数为：{}", khxxDTO);
        return customerService.KhxxFindCustomer(khxxDTO);
    }

    /**
     * Sentinel 限流/降级触发时的 blockHandler
     * <p>当 QPS 超过阈值时自动调用此方法，返回友好提示</p>
     */
    public ResultVo<Page<KhxxCustomerVo>> listKhxxPageBlockHandler(KhxxDTO khxxDTO,
                                                                     BlockException e) {
        log.warn("客户列表查询被 Sentinel 限流: {}", e.getMessage());
        return ResultVo.fail("查询请求过多，请稍后重试");
    }

    /**
     * Sentinel 业务异常 fallback
     * <p>当 listKhxxPage 抛出任意的非 BlockException 异常时调用</p>
     */
    public ResultVo<Page<KhxxCustomerVo>> listKhxxPageFallback(KhxxDTO khxxDTO,
                                                                 Throwable t) {
        log.error("客户列表查询服务异常降级: {}", t.getMessage(), t);
        return ResultVo.fail("客户服务繁忙，请稍后重试");
    }

    @PostMapping("/addCustomer")
    @ApiOperation("添加客户")
    public ResultVo<?> addCustomer(Customer customer) throws Exception {
        log.info("添加客户,参数为：{}", customer);
        return customerService.addCustomer(customer);
    }

    @GetMapping("/removeCustomer")
    @ApiOperation("删除客户")
    public ResultVo<?> removeCustomer(Long id, Integer bedId) throws Exception {
        log.info("删除客户,参数为：{}", id);
        return customerService.removeCustomer(id, bedId);
    }

    @PostMapping("/editCustomer")
    @ApiOperation("修改客户信息")
    public ResultVo<?> editCustomer(Customer customer) throws Exception {
        log.info("修改客户信息,参数为：{}", customer);
        return customerService.editCustomer(customer);
    }
}
