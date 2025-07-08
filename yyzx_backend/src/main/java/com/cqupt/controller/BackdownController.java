package com.cqupt.controller;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/30 14:33
 * @description
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.dto.BackdownDTO;
import com.cqupt.pojo.Backdown;
import com.cqupt.pojo.Bed;
import com.cqupt.pojo.Customer;
import com.cqupt.service.BackdownService;
import com.cqupt.service.BedService;
import com.cqupt.service.CustomerService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.BackdownVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/backdown")
@Api(tags = "退住管理") // swagger分组
@CrossOrigin // 解决跨域问题
public class BackdownController {
    @Autowired
    private BackdownService backdownService;
    @Autowired
    private BedService bedService;
    @Autowired
    private CustomerService customerService;

    @GetMapping("/listBackdownVo")
    @ApiOperation("查询退住详情")
    public ResultVo<Page<BackdownVo>> listBackdownVo(BackdownDTO backdownDTO) throws Exception {
        return backdownService.listBackdownVo(backdownDTO);
    }

    @PostMapping("/examineBackdown")
    @ApiOperation("审批退住")
    public ResultVo examineBackdown(Backdown backdown) throws Exception {
        Backdown bd=backdownService.getById(backdown.getId());
        //审批通过，则修改床铺状态为空闲
        if (backdown.getAuditStatus() == 1) {
            Bed bed=new Bed();
            Customer customer=customerService.getById(bd.getCustomerId());
            bed.setId(customer.getBedId());
            bed.setBedStatus(1);
            bedService.updateById(bed);
        }
        return backdownService.examineBackdown(backdown);
    }

    @GetMapping("/delBackdown")
    @ApiOperation("删除退住")
    public ResultVo delBackdown(Integer id) throws Exception {
        return backdownService.delBackdown(id);
    }

    @PostMapping("/addBackdown")
    @ApiOperation("添加退住")
    public ResultVo addBackdown(Backdown backdown) throws Exception {
        backdownService.save(backdown);
        return ResultVo.ok("添加退住成功");
    }
}
