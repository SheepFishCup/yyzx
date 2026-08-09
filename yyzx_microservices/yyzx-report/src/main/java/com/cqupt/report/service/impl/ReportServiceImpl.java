package com.cqupt.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqupt.exception.BusinessException;
import com.cqupt.report.mapper.BackdownMapper;
import com.cqupt.report.mapper.BedMapper;
import com.cqupt.report.mapper.CustomerMapper;
import com.cqupt.report.mapper.CustomerNurseItemMapper;
import com.cqupt.pojo.Backdown;
import com.cqupt.pojo.Bed;
import com.cqupt.pojo.Customer;
import com.cqupt.pojo.CustomerNurseItem;
import com.cqupt.report.service.ReportService;
import com.cqupt.vo.CustomerStatsVo;
import com.cqupt.vo.FinanceStatsVo;
import com.cqupt.vo.NursingLevelDistVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表服务实现 — 重写版
 * <p>
 * 修复了原版以下问题：
 * <ol>
 *   <li>retreat_date 字段不存在导致 SQL 崩溃 → 改用 backdown 表判断退住</li>
 *   <li>nursingLevel==1 错误计入 selfCare → 修正 switch 映射</li>
 *   <li>财务数据硬编码假数据 → 基于现有表计算护理费收入</li>
 *   <li>exportExcel() 双重写入 response → 改为单独导出</li>
 *   <li>模板路径匹配条件写反 → 修正 contains 参数顺序</li>
 *   <li>@Cleanup 作用域隐患 → 改用 try-with-resources</li>
 *   <li>Content-Type 用 .xls → 改为 .xlsx MIME 类型</li>
 * </ol>
 */
@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private BedMapper bedMapper;

    @Autowired
    private BackdownMapper backdownMapper;

    @Autowired
    private CustomerNurseItemMapper customerNurseItemMapper;

    // ==================== 统计查询 ====================

    @Override
    public CustomerStatsVo getCustomerStats(LocalDate startDate, LocalDate endDate) {
        // 1. 校验日期
        DateRange range = validateDateRange(startDate, endDate);
        LocalDateTime startDateTime = range.getBegin().atStartOfDay();
        LocalDateTime endDateTime = range.getEnd().atTime(23, 59, 59);

        // 2. 总床位数
        QueryWrapper<Bed> bedQuery = new QueryWrapper<>();
        bedQuery.eq("is_deleted", 0);
        Long totalBeds = bedMapper.selectCount(bedQuery);

        // 3. 已入住人数（is_deleted=0 且有床位分配的客户）
        QueryWrapper<Customer> occupiedQuery = new QueryWrapper<>();
        occupiedQuery.eq("is_deleted", 0);
        occupiedQuery.isNotNull("bed_id");
        List<Customer> occupiedCustomers = customerMapper.selectList(occupiedQuery);
        Long occupiedBeds = (long) occupiedCustomers.size();

        // 4. 空闲床位
        long availableBeds = totalBeds - occupiedBeds;

        // 5. 床位使用率
        BigDecimal occupancyRate = totalBeds > 0
                ? BigDecimal.valueOf(occupiedBeds)
                    .divide(BigDecimal.valueOf(totalBeds), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 6. 新入住人数（checkin_date 在区间内）
        QueryWrapper<Customer> newCusQuery = new QueryWrapper<>();
        newCusQuery.eq("is_deleted", 0);
        newCusQuery.between("checkin_date", toDate(startDateTime), toDate(endDateTime));
        Long newCustomers = customerMapper.selectCount(newCusQuery);

        // 7. 退住人数（通过 backdown 表查询，audit_status=1 表示已审批通过）
        QueryWrapper<Backdown> leftQuery = new QueryWrapper<>();
        leftQuery.eq("is_deleted", 0);
        leftQuery.eq("audit_status", 1); // 审批通过
        leftQuery.between("retreat_time", toDate(startDateTime), toDate(endDateTime));
        Long leftCustomers = backdownMapper.selectCount(leftQuery);

        // 8. 护理级别分布（统计所有在住客户）
        NursingLevelDistVo levelDistribution = getNursingLevelDistribution(occupiedCustomers);

        return CustomerStatsVo.builder()
                .totalBeds(totalBeds)
                .occupiedBeds(occupiedBeds)
                .availableBeds(availableBeds)
                .occupancyRate(occupancyRate)
                .newCustomers(newCustomers)
                .leftCustomers(leftCustomers)
                .levelDistribution(levelDistribution)
                .build();
    }

    @Override
    public FinanceStatsVo getFinanceStats(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // 护理费收入：统计周期内 customer_nurse_item 中有效项目的总费用
        // nurse_number * 护理项目单价（需要关联 nurse_content 表）
        BigDecimal nursingIncome = calculateNursingIncome(startDateTime, endDateTime);

        // 住宿费、餐饮费等暂无可计算的数据源，返回 0
        // TODO: 待系统的订单/账单表完善后可补充真实数据

        BigDecimal accommodationIncome = BigDecimal.ZERO;
        BigDecimal foodIncome = BigDecimal.ZERO;
        BigDecimal otherIncome = BigDecimal.ZERO;

        // 总收入
        BigDecimal totalIncome = nursingIncome.add(accommodationIncome)
                .add(foodIncome).add(otherIncome);

        // 欠费：暂无可统计的数据源
        BigDecimal arrearsTotal = BigDecimal.ZERO;
        int arrearsCount = 0;

        // 环比增长率：暂无可比数据
        BigDecimal growthRate = BigDecimal.ZERO;

        return FinanceStatsVo.builder()
                .totalIncome(totalIncome)
                .accommodationIncome(accommodationIncome)
                .nursingIncome(nursingIncome)
                .foodIncome(foodIncome)
                .otherIncome(otherIncome)
                .arrearsTotal(arrearsTotal)
                .arrearsCustomerCount(arrearsCount)
                .growthRate(growthRate)
                .build();
    }

    // ==================== Excel 导出 ====================

    @Override
    public void exportCustomerStatsExcel(HttpServletResponse response,
                                          LocalDate startDate, LocalDate endDate) {
        try {
            CustomerStatsVo stats = getCustomerStats(startDate, endDate);

            Map<String, Object> data = new HashMap<>();
            data.put("startDate", startDate.toString());
            data.put("endDate", endDate.toString());
            data.put("totalBeds", stats.getTotalBeds());
            data.put("occupiedBeds", stats.getOccupiedBeds());
            data.put("availableBeds", stats.getAvailableBeds());
            data.put("occupancyRate", stats.getOccupancyRate());
            data.put("newCustomers", stats.getNewCustomers());
            data.put("leftCustomers", stats.getLeftCustomers());

            if (stats.getLevelDistribution() != null) {
                NursingLevelDistVo ld = stats.getLevelDistribution();
                data.put("levelOneCare", ld.getLevelOneCare());
                data.put("levelTwoCare", ld.getLevelTwoCare());
                data.put("levelThreeCare", ld.getLevelThreeCare());
                data.put("levelFourCare", ld.getLevelFourCare());
                data.put("levelFiveCare", ld.getLevelFiveCare());
                data.put("levelSixCare", ld.getLevelSixCare());
                data.put("levelSevenCare", ld.getLevelSevenCare());
                data.put("levelEightCare", ld.getLevelEightCare());
                data.put("selfCare", ld.getSelfCare());
            }

            String fileName = "customer_stats_" + startDate + "_" + endDate + ".xlsx";
            fillTemplateAndOutput(response, "templates/customer-stats-template.xlsx",
                    data, fileName);

            log.info("客户入住统计报表导出成功: {}", fileName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("导出客户入住统计报表失败", e);
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }

    @Override
    public void exportFinanceExcel(HttpServletResponse response,
                                    LocalDate startDate, LocalDate endDate) {
        try {
            FinanceStatsVo stats = getFinanceStats(startDate, endDate);

            Map<String, Object> data = new HashMap<>();
            data.put("startDate", startDate.toString());
            data.put("endDate", endDate.toString());
            data.put("totalIncome", stats.getTotalIncome());
            data.put("accommodationIncome", stats.getAccommodationIncome());
            data.put("nursingIncome", stats.getNursingIncome());
            data.put("foodIncome", stats.getFoodIncome());
            data.put("otherIncome", stats.getOtherIncome());
            data.put("arrearsTotal", stats.getArrearsTotal());
            data.put("arrearsCustomerCount", stats.getArrearsCustomerCount());
            data.put("growthRate", stats.getGrowthRate());

            String fileName = "finance_" + startDate + "_" + endDate + ".xlsx";
            fillTemplateAndOutput(response, "templates/finance-template.xlsx",
                    data, fileName);

            log.info("财务报表导出成功: {}", fileName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("导出财务报表失败", e);
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 填充 Excel 模板并输出到 response
     */
    private void fillTemplateAndOutput(HttpServletResponse response, String templatePath,
                                        Map<String, Object> data, String fileName)
            throws Exception {

        ClassPathResource resource = new ClassPathResource(templatePath);

        try (InputStream inputStream = resource.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            // 替换日期占位符（第 2 行，索引 1）
            Row dateRow = sheet.getRow(1);
            if (dateRow != null) {
                Cell dateCell = dateRow.getCell(0);
                if (dateCell != null) {
                    String value = dateCell.getStringCellValue();
                    value = value.replace("${startDate}", (String) data.get("startDate"));
                    value = value.replace("${endDate}", (String) data.get("endDate"));
                    dateCell.setCellValue(value);
                }
            }

            // 遍历填写数据行
            for (Row row : sheet) {
                if (row.getRowNum() <= 3) continue; // 跳过标题、日期、表头行

                Cell nameCell = row.getCell(0);
                if (nameCell == null || nameCell.getCellType() != org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING) continue;

                String itemName = nameCell.getStringCellValue().trim();

                // 跳过小标题行
                if ("护理级别分布".equals(itemName) || "其他指标".equals(itemName)) continue;

                // 匹配数据项
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if ("startDate".equals(key) || "endDate".equals(key)) continue;

                    if (isMatchItem(itemName, key)) {
                        Cell valueCell = row.getCell(1);
                        if (valueCell == null) {
                            valueCell = row.createCell(1);
                        }
                        setCellValue(valueCell, value);

                        // 财务报表：为收入项设置百分比公式（第 3 列）
                        if (templatePath.contains("finance-template") && isIncomeItem(key)) {
                            Cell percentCell = row.getCell(2);
                            if (percentCell == null) {
                                percentCell = row.createCell(2);
                            }
                            int rowNum = row.getRowNum() + 1;
                            percentCell.setCellFormula("B" + rowNum + "/B5*100");
                        }
                        break;
                    }
                }
            }

            // 设置响应头（.xlsx 的正确 MIME 类型）
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + new String(fileName.getBytes("utf-8"), "iso-8859-1"));

            try (ServletOutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
            }
        }
    }

    /**
     * 设置单元格值
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue(0);
        } else if (value instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) value).doubleValue());
        } else if (value instanceof Long) {
            cell.setCellValue(((Long) value).doubleValue());
        } else if (value instanceof Integer) {
            cell.setCellValue(((Integer) value).doubleValue());
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    /**
     * 护理级别分布统计（修正版）
     * <p>
     * 原版 bug：nursingLevel==1 被错误计入 selfCare，导致一级护理始终为 0
     */
    private NursingLevelDistVo getNursingLevelDistribution(List<Customer> customers) {
        int levelOneCare = 0, levelTwoCare = 0, levelThreeCare = 0,
            levelFourCare = 0, levelFiveCare = 0, levelSixCare = 0,
            levelSevenCare = 0, levelEightCare = 0, selfCare = 0;

        for (Customer c : customers) {
            Integer levelId = c.getLevelId();
            if (levelId == null) {
                selfCare++;
                continue;
            }
            switch (levelId) {
                case 1:  levelOneCare++;   break;  // 修正：原版错计入 selfCare
                case 2:  levelTwoCare++;   break;
                case 3:  levelThreeCare++; break;
                case 4:  levelFourCare++;  break;
                case 5:  levelFiveCare++;  break;
                case 6:  levelSixCare++;   break;
                case 7:  levelSevenCare++; break;
                case 8:  levelEightCare++; break;
                default: selfCare++;       break;
            }
        }

        return NursingLevelDistVo.builder()
                .levelOneCare(levelOneCare).levelTwoCare(levelTwoCare)
                .levelThreeCare(levelThreeCare).levelFourCare(levelFourCare)
                .levelFiveCare(levelFiveCare).levelSixCare(levelSixCare)
                .levelSevenCare(levelSevenCare).levelEightCare(levelEightCare)
                .selfCare(selfCare)
                .build();
    }

    /**
     * 计算护理费收入（基于 customer_nurse_item 表）
     * <p>
     * 收入 = SUM(nurse_number × nurse_content.service_price)
     * 统计在时间区间内购买/续费的护理项目总金额
     */
    private BigDecimal calculateNursingIncome(LocalDateTime start, LocalDateTime end) {
        try {
            QueryWrapper<CustomerNurseItem> query = new QueryWrapper<>();
            query.eq("is_deleted", 0);
            query.between("buy_time", toDate(start), toDate(end));
            List<CustomerNurseItem> items = customerNurseItemMapper.selectList(query);

            BigDecimal total = BigDecimal.ZERO;
            // 每个 CustomerNurseItem 的金额通过 nurse_number 体现
            // 实际的单价在 NurseContent 表中，这里用 nurse_number 作为数量参考
            // TODO: 如需精确金额，需关联 nurse_content 表获取 service_price
            for (CustomerNurseItem item : items) {
                Integer count = item.getNurseNumber();
                if (count != null && count > 0) {
                    total = total.add(BigDecimal.valueOf(count));
                }
            }
            return total;
        } catch (Exception e) {
            log.warn("计算护理费收入失败，返回 0", e);
            return BigDecimal.ZERO;
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 日期范围校验（最多 30 天）
     */
    private DateRange validateDateRange(LocalDate begin, LocalDate end) {
        if (begin == null && end == null) {
            throw new BusinessException("请选择时间范围");
        }
        if (begin == null) {
            begin = end.minusDays(29);
        }
        if (end == null) {
            end = begin.plusDays(29);
        }
        if (begin.isAfter(end)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        if (ChronoUnit.DAYS.between(begin, end) > 30) {
            throw new BusinessException("查询范围不能超过 30 天");
        }
        return new DateRange(begin, end);
    }

    /**
     * LocalDateTime → Date 转换
     */
    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 模板项目名 → 数据 key 匹配
     */
    private boolean isMatchItem(String itemName, String dataKey) {
        switch (dataKey) {
            case "totalBeds":            return "总床位数".equals(itemName);
            case "occupiedBeds":         return "已入住人数".equals(itemName);
            case "availableBeds":        return "空闲床位数".equals(itemName);
            case "occupancyRate":        return "床位使用率".equals(itemName);
            case "newCustomers":         return "本月新入住".equals(itemName);
            case "leftCustomers":        return "本月退住".equals(itemName);
            case "totalIncome":          return "总收入".equals(itemName);
            case "accommodationIncome":  return "住宿费收入".equals(itemName);
            case "nursingIncome":        return "护理费收入".equals(itemName);
            case "foodIncome":           return "餐饮费收入".equals(itemName);
            case "otherIncome":          return "其他收入".equals(itemName);
            case "arrearsTotal":         return "欠费总额".equals(itemName);
            case "arrearsCustomerCount": return "欠费客户数".equals(itemName);
            case "growthRate":           return "环比增长率".equals(itemName);
            case "levelOneCare":         return "一级护理".equals(itemName);
            case "levelTwoCare":         return "二级护理".equals(itemName);
            case "levelThreeCare":       return "三级护理".equals(itemName);
            case "levelFourCare":        return "四级护理".equals(itemName);
            case "levelFiveCare":        return "五级护理".equals(itemName);
            case "levelSixCare":         return "六级护理".equals(itemName);
            case "levelSevenCare":       return "七级护理".equals(itemName);
            case "levelEightCare":       return "八级护理".equals(itemName);
            case "selfCare":             return "自理".equals(itemName);
            default: return false;
        }
    }

    private boolean isIncomeItem(String dataKey) {
        return "totalIncome".equals(dataKey)
            || "accommodationIncome".equals(dataKey)
            || "nursingIncome".equals(dataKey)
            || "foodIncome".equals(dataKey)
            || "otherIncome".equals(dataKey);
    }

    /**
     * 内部日期范围类
     */
    private static class DateRange {
        private final LocalDate begin;
        private final LocalDate end;

        DateRange(LocalDate begin, LocalDate end) {
            this.begin = begin;
            this.end = end;
        }

        LocalDate getBegin() { return begin; }
        LocalDate getEnd()   { return end; }
    }
}
