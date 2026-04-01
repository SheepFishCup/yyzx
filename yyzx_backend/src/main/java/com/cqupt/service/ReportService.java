package com.cqupt.service;


import com.cqupt.vo.CustomerStatsVo;
import com.cqupt.vo.FinanceStatsVo;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {
    // 导出报表
    void exportExcel(HttpServletResponse httpServletResponse);

    void exportCustomerStatsExcel(HttpServletResponse response, LocalDate startDate, LocalDate endDate);

    void exportFinanceExcel(HttpServletResponse response, LocalDate startDate, LocalDate endDate);

    /**
     * 获取客户入住统计数据
     */
    CustomerStatsVo getCustomerStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取财务统计数据
     */
    FinanceStatsVo getFinanceStats(LocalDate startDate, LocalDate endDate);

}
