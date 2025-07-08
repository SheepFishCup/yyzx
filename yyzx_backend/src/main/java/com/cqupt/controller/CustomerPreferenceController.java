package com.cqupt.controller;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/27 09:35
 * @description
 */

import com.cqupt.dto.CustomerPreferenceDTO;
import com.cqupt.pojo.CustomerPreference;
import com.cqupt.service.CustomerPreferenceService;
import com.cqupt.utils.ResultVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customerpreference")
@Api(tags = "顾客喜好管理") // swagger分组
@CrossOrigin
public class CustomerPreferenceController {
    @Autowired
    private CustomerPreferenceService customerPreferenceService;

    @PostMapping("/addCustomerpreference")
    @ApiOperation("为顾客单个添加喜好")
    public ResultVo addCustomerpreference(CustomerPreference customerPreference) throws Exception {
        customerPreferenceService.save(customerPreference);
        return ResultVo.ok("添加成功");
    }

    @PostMapping("/updateCustomerpreference")
    @ApiOperation("为顾客单个修改喜好")
    public ResultVo updateCustomerpreference(CustomerPreference customerPreference) throws Exception {
        customerPreferenceService.updateById(customerPreference);
        return ResultVo.ok("修改成功");
    }

    @GetMapping("/removeCustomerpreference")
    @ApiOperation("为顾客单个删除喜好")
    public ResultVo removeCustomerpreference(Integer id) throws Exception {
        customerPreferenceService.removeById(id);
        return ResultVo.ok("删除成功");
    }

    @GetMapping("/listCustomerpreference")
    @ApiOperation("查询顾客喜好")
    public ResultVo listCustomerpreference(CustomerPreferenceDTO customerPreferenceDTO) throws Exception {
        return customerPreferenceService.listCustomerPreferenceVoPage(customerPreferenceDTO);
    }

}
