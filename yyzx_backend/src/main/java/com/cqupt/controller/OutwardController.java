package com.cqupt.controller;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/30 17:13
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.dto.OutwardDTO;
import com.cqupt.pojo.Bed;
import com.cqupt.pojo.Customer;
import com.cqupt.pojo.Outward;
import com.cqupt.service.BedService;
import com.cqupt.service.CustomerService;
import com.cqupt.service.OutwardService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.OutwardVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/outward")
@Api(tags = "外出管理") // swagger分组
@CrossOrigin // 解决跨域问题
public class OutwardController {
    @Autowired
    private OutwardService outwardService;
    @Autowired
    private BedService bedService;
    @Autowired
    private CustomerService customerService;
    @GetMapping("/queryOutwardVo")
    @ApiOperation("查询外出详情")
    public ResultVo<Page<OutwardVo>> queryOutwardVo(OutwardDTO outwardDTO) throws Exception {
        return outwardService.queryOutwardVo(outwardDTO);
    }
    @PostMapping("/examineOutward")
    @ApiOperation("审批外出")
    public ResultVo examineOutward(Outward outward) throws Exception {
        Outward ow=outwardService.getById(outward.getId());
        if (ow.getAuditStatus()==1){
            Customer customer=customerService.getById(ow.getCustomerId());
            Bed bed=new Bed();
            bed.setId(customer.getBedId());
            bed.setBedStatus(3);
            bedService.updateById(bed);
        }
        return outwardService.examineOutward(outward);
    }
    @GetMapping("/delOutward")
    @ApiOperation("删除退住")
    public ResultVo delOutward(Integer id) throws Exception {
        return outwardService.delOutward(id);
    }
    @PostMapping("/updateOutward")
    @ApiOperation("修改退住")
    public ResultVo updateOutward(Outward outward) throws Exception {
        return outwardService.updateOutward(outward);
    }
    @PostMapping("/addOutward")
    @ApiOperation("添加退住")
    public ResultVo addOutward(Outward outward) throws Exception {
        outwardService.save(outward);
//        outwardService.addOutward(outward);
        return ResultVo.ok("添加成功");
    }

    @PostMapping("/updateBackTime")
    @ApiOperation("登记回院时间")
    public ResultVo updateBackTime(Outward outward) throws Exception {
        UpdateWrapper<Outward> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",outward.getId());
        outwardService.updateById(outward);
        return ResultVo.ok("登记时间成功");
    }

}
