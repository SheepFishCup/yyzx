package com.cqupt.report.controller;

import com.cqupt.report.service.ReportService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.CustomerStatsVo;
import com.cqupt.vo.FinanceStatsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * 报表管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/report")
@Api(tags = "报表管理")
@CrossOrigin
public class ReportController {

    @Autowired
    private ReportService reportService;

    @ApiOperation("获取客户入住统计数据")
    @GetMapping("/customerStats")
    public ResultVo<CustomerStatsVo> getCustomerStats(
            @ApiParam("开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @ApiParam("结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("查询客户统计: {} ~ {}", startDate, endDate);
        CustomerStatsVo stats = reportService.getCustomerStats(startDate, endDate);
        return ResultVo.ok(stats);
    }

    @ApiOperation("获取财务统计数据")
    @GetMapping("/financeStats")
    public ResultVo<FinanceStatsVo> getFinanceStats(
            @ApiParam("开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @ApiParam("结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("查询财务统计: {} ~ {}", startDate, endDate);
        FinanceStatsVo stats = reportService.getFinanceStats(startDate, endDate);
        return ResultVo.ok(stats);
    }

    @ApiOperation("导出客户入住统计 Excel")
    @GetMapping("/exportCustomerExcel")
    public void exportCustomerExcel(
            @ApiParam("开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @ApiParam("结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) {
        log.info("导出客户统计 Excel: {} ~ {}", startDate, endDate);
        reportService.exportCustomerStatsExcel(response, startDate, endDate);
    }

    @ApiOperation("导出财务统计 Excel")
    @GetMapping("/exportFinanceExcel")
    public void exportFinanceExcel(
            @ApiParam("开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @ApiParam("结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) {
        log.info("导出财务 Excel: {} ~ {}", startDate, endDate);
        reportService.exportFinanceExcel(response, startDate, endDate);
    }
}
