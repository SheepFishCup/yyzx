package com.cqupt.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/20 23:07
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqupt.exception.BusinessException;
import com.cqupt.mapper.BedMapper;
import com.cqupt.mapper.CustomerMapper;
import com.cqupt.pojo.Bed;
import com.cqupt.pojo.Customer;
import com.cqupt.service.ReportService;
import com.cqupt.vo.CustomerStatsVo;
import com.cqupt.vo.FinanceStatsVo;
import com.cqupt.vo.NursingLevelDistVo;
import lombok.Cleanup;
import lombok.Data;
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
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Slf4j
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private BedMapper bedMapper;

    @Override
    public void exportExcel(HttpServletResponse response) {
        // 默认导出客户入住统计
        exportCustomerStatsExcel(response, LocalDate.now().minusMonths(1), LocalDate.now());
        // 默认导出财务报表
        exportFinanceExcel(response, LocalDate.now().minusMonths(1), LocalDate.now());

    }

    @Override
    public void exportCustomerStatsExcel(HttpServletResponse response, LocalDate startDate, LocalDate endDate) {
        try {
            // 获取统计数据
            CustomerStatsVo stats = getCustomerStats(startDate, endDate);

            // 准备模板数据
            Map<String, Object> data = new HashMap<>();
            data.put("startDate", startDate.toString());
            data.put("endDate", endDate.toString());
            data.put("totalBeds", stats.getTotalBeds());
            data.put("occupiedBeds", stats.getOccupiedBeds());
            data.put("availableBeds", stats.getAvailableBeds());
            data.put("occupancyRate", stats.getOccupancyRate());
            data.put("newCustomers", stats.getNewCustomers());
            data.put("leftCustomers", stats.getLeftCustomers());

            // 护理级别分布
            if (stats.getLevelDistribution() != null) {
                NursingLevelDistVo levelDist = stats.getLevelDistribution();
                data.put("levelOneCare", levelDist.getLevelOneCare());
                data.put("levelTwoCare", levelDist.getLevelTwoCare());
                data.put("levelThreeCare", levelDist.getLevelThreeCare());
                data.put("levelFourCare", levelDist.getLevelFourCare());
                data.put("levelFiveCare", levelDist.getLevelFiveCare());
                data.put("levelSixCare", levelDist.getLevelSixCare());
                data.put("levelSevenCare", levelDist.getLevelSevenCare());
                data.put("levelEightCare", levelDist.getLevelEightCare());
                data.put("selfCare", levelDist.getSelfCare());
            }

            // 加载模板并填充数据
            fillTemplateAndOutput(response, "templates/customer-stats-template.xlsx", data,
                    "customer_stats_" + startDate + "_" + endDate + ".xlsx");

        } catch (Exception e) {
            log.error("导出客户入住统计报表失败", e);
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }

    @Override
    public void exportFinanceExcel(HttpServletResponse response, LocalDate startDate, LocalDate endDate) {
        try {
            // 获取统计数据
            FinanceStatsVo stats = getFinanceStats(startDate, endDate);

            // 准备模板数据
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

            // 加载模板并填充数据
            fillTemplateAndOutput(response, "templates/finance-template.xlsx", data,
                    "finance_" + startDate + "_" + endDate + ".xlsx");

        } catch (Exception e) {
            log.error("导出财务报表失败", e);
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }

    @Override
    public CustomerStatsVo getCustomerStats(LocalDate startDate, LocalDate endDate) {
        // 校验日期范围
        DateRange range = checkDate(startDate, endDate);
        LocalDateTime startDateTime = range.getBegin().atStartOfDay();
        LocalDateTime endDateTime = range.getEnd().atTime(23, 59, 59);

        // 查询总床位数
        QueryWrapper<Bed> bedQuery = new QueryWrapper<>();
        bedQuery.eq("is_deleted", 0);
        Integer totalBeds = bedMapper.selectCount(bedQuery);

        // 查询已入住人数（通过客户表查询在住客户）
        QueryWrapper<Customer> customerQuery = new QueryWrapper<>();
        customerQuery.eq("is_deleted", 0);
        customerQuery.isNull("retreat_date"); // 未退住的客户

        List<Customer> customers = customerMapper.selectList(customerQuery);
        Integer occupiedBeds = customers.size();

        // 计算空闲床位
        Integer availableBeds = totalBeds - occupiedBeds;

        // 计算床位使用率
        BigDecimal occupancyRate = totalBeds > 0
                ? BigDecimal.valueOf(occupiedBeds).divide(BigDecimal.valueOf(totalBeds), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 查询指定时间段内新入住人数
        QueryWrapper<Customer> newCustomerQuery = new QueryWrapper<>();
        newCustomerQuery.eq("is_deleted", 0);
        newCustomerQuery.between("checkin_date", startDateTime, endDateTime);
        Integer newCustomers = customerMapper.selectCount(newCustomerQuery);

        // 查询指定时间段内退住人数
        QueryWrapper<Customer> leftCustomerQuery = new QueryWrapper<>();
        leftCustomerQuery.eq("is_deleted", 0);
        leftCustomerQuery.isNotNull("retreat_date");
        leftCustomerQuery.between("retreat_date", startDateTime, endDateTime);
        Integer leftCustomers = customerMapper.selectCount(leftCustomerQuery);

        // 查询护理级别分布
        NursingLevelDistVo levelDistribution = getNursingLevelDistribution(customers);

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
        // TODO: 根据实际的订单/收费表实现财务统计
        // 这里提供示例数据，实际需要从数据库查询

        return FinanceStatsVo.builder()
                .totalIncome(BigDecimal.valueOf(100000.00))
                .accommodationIncome(BigDecimal.valueOf(50000.00))
                .nursingIncome(BigDecimal.valueOf(30000.00))
                .foodIncome(BigDecimal.valueOf(15000.00))
                .otherIncome(BigDecimal.valueOf(5000.00))
                .arrearsTotal(BigDecimal.valueOf(8000.00))
                .arrearsCustomerCount(5)
                .growthRate(BigDecimal.valueOf(12.5))
                .build();
    }

    /**
     * 填充模板并输出
     */
    private void fillTemplateAndOutput(HttpServletResponse response, String templatePath,
                                       Map<String, Object> data, String fileName) throws Exception {
        // 加载模板文件
        ClassPathResource resource = new ClassPathResource(templatePath);
        @Cleanup InputStream inputStream = resource.getInputStream();
        @Cleanup Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheetAt(0);

        // 替换日期占位符（第 2 行）
        Row dateRow = sheet.getRow(1);
        if (dateRow != null) {
            Cell dateCell = dateRow.getCell(0);
            if (dateCell != null) {
                String value = dateCell.getStringCellValue();
                value = value.replace("${startDate}", (String)data.get("startDate"));
                value = value.replace("${endDate}", (String)data.get("endDate"));
                dateCell.setCellValue(value);
            }
        }

        // 遍历所有行，填充数据
        for (Row row : sheet) {
            // 跳过标题行、日期行和表头行
            if (row.getRowNum() <= 3) continue;

            Cell nameCell = row.getCell(0);
            if (nameCell == null || nameCell.getCellTypeEnum() != CellType.STRING) continue;

            String itemName = nameCell.getStringCellValue();

            // 跳过小标题行
            if ("护理级别分布".equals(itemName) || "其他指标".equals(itemName)) continue;

            // 根据项目名称匹配对应的数据
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // 跳过日期字段
                if ("startDate".equals(key) || "endDate".equals(key)) continue;

                // 匹配项目名称
                if (isMatchItem(itemName, key)) {
                    // 填充数值单元格（第 2 列）
                    Cell valueCell = row.getCell(1);
                    if (valueCell == null) {
                        valueCell = row.createCell(1);
                    }

                    if (value instanceof BigDecimal) {
                        valueCell.setCellValue(((BigDecimal) value).doubleValue());
                    } else if (value instanceof Integer) {
                        valueCell.setCellValue(((Integer) value).doubleValue());
                    } else if (value instanceof Number) {
                        valueCell.setCellValue(((Number) value).doubleValue());
                    }

                    // 为财务报表的占比列设置公式
                    if ("finance-template.xlsx".contains(templatePath) &&
                            isIncomeItem(key)) {
                        Cell percentCell = row.getCell(2);
                        if (percentCell == null) {
                            percentCell = row.createCell(2);
                        }
                        // 设置百分比公式：当前行 B 列 / 总收入 B 列 * 100
                        String formula = "B" + (row.getRowNum() + 1) + "/$B$5*100";
                        percentCell.setCellFormula(formula);
                    }
                    break;
                }
            }
        }

        // 输出 Excel
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        @Cleanup ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
    }

    /**
     * 判断是否为收入项目（需要计算占比）
     */
    private boolean isIncomeItem(String dataKey) {
        return "totalIncome".equals(dataKey) ||
                "accommodationIncome".equals(dataKey) ||
                "nursingIncome".equals(dataKey) ||
                "foodIncome".equals(dataKey) ||
                "otherIncome".equals(dataKey);
    }

    /**
     * 判断项目名称是否匹配数据 key
     */
    private boolean isMatchItem(String itemName, String dataKey) {
        switch (dataKey) {
            case "totalIncome": return "总收入".equals(itemName);
            case "accommodationIncome": return "住宿费收入".equals(itemName);
            case "nursingIncome": return "护理费收入".equals(itemName);
            case "foodIncome": return "餐饮费收入".equals(itemName);
            case "otherIncome": return "其他收入".equals(itemName);
            case "arrearsTotal": return "欠费总额".equals(itemName);
            case "arrearsCustomerCount": return "欠费客户数".equals(itemName);
            case "growthRate": return "环比增长率".equals(itemName);
            case "totalBeds": return "总床位数".equals(itemName);
            case "occupiedBeds": return "已入住人数".equals(itemName);
            case "availableBeds": return "空闲床位数".equals(itemName);
            case "occupancyRate": return "床位使用率".equals(itemName);
            case "newCustomers": return "本月新入住".equals(itemName);
            case "leftCustomers": return "本月退住".equals(itemName);
            case "levelOneCare": return "一级护理".equals(itemName);
            case "levelTwoCare": return "二级护理".equals(itemName);
            case "levelThreeCare": return "三级护理".equals(itemName);
            case "levelFourCare": return "四级护理".equals(itemName);
            case "levelFiveCare": return "五级护理".equals(itemName);
            case "levelSixCare": return "六级护理".equals(itemName);
            case "levelSevenCare": return "七级护理".equals(itemName);
            case "levelEightCare": return "八级护理".equals(itemName);
            case "selfCare": return "自理".equals(itemName);
            default: return false;
        }
    }

    /**
     * 获取护理级别分布
     */
    private NursingLevelDistVo getNursingLevelDistribution(List<Customer> customers) {
        int levelOneCare = 0;
        int levelTwoCare = 0;
        int levelThreeCare = 0;
        int levelFourCare = 0;
        int levelFiveCare = 0;
        int levelSixCare = 0;
        int levelSevenCare = 0;
        int levelEightCare = 0;
        int selfCare = 0;

        for (Customer customer : customers) {
            // 判断是否为自理
            Integer nursingLevel = customer.getLevelId();
            if (nursingLevel == null) {
                selfCare++;
                continue;
            }

            switch (nursingLevel) {
                case 1: selfCare++; break;
                case 2: levelTwoCare++; break;
                case 3: levelThreeCare++; break;
                case 4: levelFourCare++; break;
                case 5: levelFiveCare++; break;
                case 6: levelSixCare++; break;
                case 7: levelSevenCare++; break;
                case 8: levelEightCare++; break;
                default: selfCare++;
            }
        }

        return NursingLevelDistVo.builder()
                .levelOneCare(levelOneCare)
                .levelTwoCare(levelTwoCare)
                .levelThreeCare(levelThreeCare)
                .levelFourCare(levelFourCare)
                .levelFiveCare(levelFiveCare)
                .levelSixCare(levelSixCare)
                .levelSevenCare(levelSevenCare)
                .levelEightCare(levelEightCare)
                .selfCare(selfCare)
                .build();
    }

    /**
     * 校验日期范围
     */
    private DateRange checkDate(LocalDate begin, LocalDate end) {
        if (begin == null && end == null) {
            throw new BusinessException("范围过大，请限定时间范围");
        }
        if (begin == null) {
            begin = end.minusDays(29);
        }
        if (end == null) {
            end = begin.plusDays(29);
        }
        if (begin.isAfter(end)) {
            throw new BusinessException("时间选择错误");
        }
        if (ChronoUnit.DAYS.between(begin, end) > 30) {
            throw new BusinessException("时间超出 30 天，暂不支持查询");
        }
        return new DateRange(begin, end);
    }

    @Data
    static class DateRange {
        private final LocalDate begin;
        private final LocalDate end;
    }
}
