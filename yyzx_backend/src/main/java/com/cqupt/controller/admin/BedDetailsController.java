package com.cqupt.controller.admin;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@Slf4j
@RestController
//@RequestMapping("/admin/beddetails")
@RequestMapping("/beddetails")
@Api(tags = "床位详情管理")
@CrossOrigin // 解决跨域问题
public class BedDetailsController {
    @Autowired
    BedDetailsService bedDetailsService;

    @GetMapping("/listBedDetailsVoPage")
    @ApiOperation("床位详情列表动态查询（分页）")
    public ResultVo<Page<BedDetailsVo>> listBedDetailsVoPage(@Valid BedDetailsDTO bedDetailsDTO) throws Exception {
        log.info("查询床位详情列表（分页）,参数：{}", bedDetailsDTO);
        return bedDetailsService.listBedDetailsVoPage(bedDetailsDTO);
    }

//    @PostMapping("/updateBedDetails")
    @PutMapping
    @ApiOperation("更新床位使用情况-只能修改床位使用结束时间")
    public ResultVo updateBedDetails(@RequestBody BedDetails bedDetails) throws Exception {
        log.info("更新床位使用情况,参数为：{}", bedDetails);
        bedDetailsService.updateById(bedDetails);
        return ResultVo.ok("编辑成功");
    }

//    @GetMapping("/delBedDetails")
    @DeleteMapping
    @ApiOperation("删除床位记录")
    public ResultVo delBedDetails(@RequestParam @NotEmpty(message = "id不能为空") Long id) throws Exception {
        log.info("删除床位记录,参数为：{}", id);
        bedDetailsService.removeById(id);
        return ResultVo.ok("删除成功");
    }
    @PostMapping("/exchangeBed")
    @ApiOperation("床位调换")
    public ResultVo exchangeBed(@RequestBody ExchangeDTO exchangeDTO) throws Exception {
        log.info("床位调换,参数为：{}", exchangeDTO);
        return bedDetailsService.exchangeBed(exchangeDTO);
    }
}
