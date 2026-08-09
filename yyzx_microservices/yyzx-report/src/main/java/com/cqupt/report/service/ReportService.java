package com.cqupt.report.service;

import com.cqupt.vo.CustomerStatsVo;
import com.cqupt.vo.FinanceStatsVo;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * 报表服务接口
 */
public interface ReportService {

    /**
     * 获取客户入住统计数据
     */
    CustomerStatsVo getCustomerStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取财务统计数据
     */
    FinanceStatsVo getFinanceStats(LocalDate startDate, LocalDate endDate);

    /**
     * 导出客户入住统计 Excel
     */
    void exportCustomerStatsExcel(HttpServletResponse response, LocalDate startDate, LocalDate endDate);

    /**
     * 导出财务统计 Excel
     */
    void exportFinanceExcel(HttpServletResponse response, LocalDate startDate, LocalDate endDate);
}
