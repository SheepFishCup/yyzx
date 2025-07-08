package com.cqupt.controller;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/26 10:35
 * @description
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.dto.KhxxDTO;
import com.cqupt.pojo.Customer;
import com.cqupt.service.CustomerService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.KhxxCustomerVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
@Api(tags = "客户管理") // swagger分组
@CrossOrigin
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping("/listKhxxPage")
    @ApiOperation("客户信息列表查询(分页)")
    public ResultVo<Page<KhxxCustomerVo>> listKhxxPage(KhxxDTO khxxDTO) throws Exception {
        return customerService.KhxxFindCustomer(khxxDTO);
    }
    @PostMapping("/addCustomer")
    @ApiOperation("添加客户")
    public ResultVo addCustomer(Customer customer) throws Exception {
        return customerService.addCustomer(customer);
    }

    @GetMapping("/removeCustomer")
    @ApiOperation("删除客户")
    public ResultVo removeCustomer(Integer id,Integer bedId) throws Exception {
        return customerService.removeCustomer(id,bedId);
    }

    @PostMapping("/editCustomer")
    @ApiOperation("修改客户信息")
    public ResultVo editCustomer(Customer customer) throws Exception {
        return customerService.editCustomer(customer);
    }

}
