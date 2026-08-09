package com.cqupt.customer.controller;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/27 14:47
 * @description
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.dto.CustomerNurseItemDTO;
import com.cqupt.pojo.CustomerNurseItem;
import com.cqupt.customer.service.CustomerNurseItemService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.CustomerNurseItemVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
//@RequestMapping("/admin/customernurseitem")
@RequestMapping("/customernurseitem")
@Api(tags = "用户护理项目管理") // swagger分组
@CrossOrigin
public class CustomerNurseItemController {
    @Autowired
    private CustomerNurseItemService customerNurseItemService;

    @GetMapping("/listCustomerItem")
    @ApiOperation("查询顾客护理项目列表-分页")
    public ResultVo<Page<CustomerNurseItemVo>> listCustomerItem(CustomerNurseItemDTO customerNurseItemDTO) throws Exception {
        log.info("查询顾客护理项目列表-分页,参数为：{}",customerNurseItemDTO);
        return customerNurseItemService.listCustomerItem(customerNurseItemDTO);
    }

    @PostMapping("/addItemToCustomer")
    @ApiOperation("为顾客单个/批量添加护理项目")
    public ResultVo addItemToCustomer(@RequestBody @Valid List<CustomerNurseItem> customerNurseItems) throws Exception {
        log.info("为顾客单个/批量添加护理项目,参数为：{}", customerNurseItems);
        return customerNurseItemService.addItemToCustomer(customerNurseItems);
    }

    @GetMapping("/removeCustomerLevelAndItem")
    @ApiOperation("移除客户护理级别，移除用户当前级别的护理项目")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType="int",name = "levelId", value = "护理级别编号",required = true),
            @ApiImplicitParam(dataType="Long",name = "customerId", value = "用户编号",required = true)
    })
    public ResultVo removeCustomerLevelAndItem(Integer levelId, Long customerId) throws Exception {
        log.info("移除客户护理级别，移除用户当前级别的护理项目,参数为：{}，{}", levelId, customerId);
        return customerNurseItemService.removeCustomerLevelAndItem(levelId, customerId);
    }

    @PostMapping("/enewNurseItem")
    @ApiOperation("客户续费")
    public ResultVo enewNurseItem(@RequestBody CustomerNurseItem customerNurseItem) throws Exception {
        log.info("客户续费,参数为：{}", customerNurseItem);
        return customerNurseItemService.enewNurseItem(customerNurseItem);
    }

    @GetMapping("/isIncludesItemCustomer")
    @ApiOperation("判断用户是否配置了某个护理项目")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType="int",name = "itemId", value = "护理项目编号",required = true),
            @ApiImplicitParam(dataType="Long",name = "customerId", value = "用户编号",required = true)
    })
    public ResultVo isIncludesItemCustomer(Integer itemId, Long customerId) throws Exception {
        log.info("判断用户是否配置了某个护理项目,参数为：{}，{}", itemId, customerId);
        return customerNurseItemService.isIncludesItemCustomer(itemId, customerId);
    }

    @GetMapping("/removeCustomerItem")
    @ApiOperation("移除用户护理项目")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType="Long",name = "id", value = "主键key",required = true)
    })
    public ResultVo removeCustomerItem(Long id) throws Exception {
        log.info("移除用户护理项目,参数为：{}", id);
        return customerNurseItemService.removeCustomerItem(id);
    }
}
