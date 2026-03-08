package com.cqupt.controller.admin;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/27 09:35
 * @description
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.dto.CustomerPreferenceDTO;
import com.cqupt.pojo.CustomerPreference;
import com.cqupt.service.CustomerPreferenceService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.CustomerPreferenceVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
//@RequestMapping("/admin/customerpreference")
@RequestMapping("/customerpreference")
@Api(tags = "顾客喜好管理") // swagger分组
@CrossOrigin
public class CustomerPreferenceController {
    @Autowired
    private CustomerPreferenceService customerPreferenceService;

    @PostMapping("/addCustomerpreference")
    @ApiOperation("为顾客单个添加喜好")
    public ResultVo addCustomerpreference(CustomerPreference customerPreference) throws Exception {
        log.info("添加顾客喜好,参数为：{}", customerPreference);
        customerPreferenceService.save(customerPreference);
        return ResultVo.ok("添加成功");
    }

    @PostMapping("/updateCustomerpreference")
    @ApiOperation("为顾客单个修改喜好")
    public ResultVo updateCustomerpreference(CustomerPreference customerPreference) throws Exception {
        log.info("修改顾客喜好,参数为：{}", customerPreference);
        customerPreferenceService.updateById(customerPreference);
        return ResultVo.ok("修改成功");
    }

    @GetMapping("/removeCustomerpreference")
    @ApiOperation("为顾客单个删除喜好")
    public ResultVo removeCustomerpreference(Long id) throws Exception {
        log.info("删除顾客喜好,参数为：{}", id);
        customerPreferenceService.removeById(id);
        return ResultVo.ok("删除成功");
    }

    @GetMapping("/listCustomerpreference")
    @ApiOperation("查询顾客喜好")
    public ResultVo<Page<CustomerPreferenceVo>> listCustomerpreference(CustomerPreferenceDTO customerPreferenceDTO) throws Exception {
        log.info("查询顾客喜好,参数为：{}", customerPreferenceDTO);
        return customerPreferenceService.listCustomerPreferenceVoPage(customerPreferenceDTO);
    }

}
