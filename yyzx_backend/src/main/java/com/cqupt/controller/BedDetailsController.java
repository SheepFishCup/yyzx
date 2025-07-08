package com.cqupt.controller;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/24 16:17
 * @description
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.dto.BedDetailsDTO;
import com.cqupt.dto.ExchangeDTO;
import com.cqupt.pojo.BedDetails;
import com.cqupt.service.BedDetailsService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.BedDetailsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/beddetails")
@Api(tags = "床位详情管理")
@CrossOrigin // 解决跨域问题
public class BedDetailsController {
    @Autowired
    BedDetailsService bedDetailsService;

    @GetMapping("/listBedDetailsVoPage")
    @ApiOperation("床位详情列表动态查询（分页）")
    public ResultVo<Page<BedDetailsVo>> listBedDetailsVoPage(BedDetailsDTO bedDetailsDTO) throws Exception {
        return bedDetailsService.listBedDetailsVoPage(bedDetailsDTO);
    }

    @PostMapping("/updateBedDetails")
    @ApiOperation("更新床位使用情况-只能修改床位使用结束时间")
    public ResultVo updateBedDetails(BedDetails bedDetails) throws Exception {
        bedDetailsService.updateById(bedDetails);
        return ResultVo.ok("编辑成功");
    }

    @GetMapping("/delBedDetails")
    @ApiOperation("删除记录")
    public ResultVo delBedDetails(Integer id) throws Exception {
        bedDetailsService.removeById(id);
        return ResultVo.ok("删除成功");
    }
    @PostMapping("/exchangeBed")
    @ApiOperation("床位调换")
    public ResultVo exchangeBed(ExchangeDTO exchangeDTO) throws Exception {
        return bedDetailsService.exchangeBed(exchangeDTO);
    }
}
