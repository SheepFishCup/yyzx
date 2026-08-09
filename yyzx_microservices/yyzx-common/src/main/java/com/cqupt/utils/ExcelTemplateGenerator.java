package com.cqupt.utils;

import lombok.Cleanup;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Excel 模板生成工具类
 * 用于初始化报表模板（只需运行一次）
 */
@Component
public class ExcelTemplateGenerator {

    /**
     * 生成客户入住统计模板
     */
    public static void createCustomerStatsTemplate(String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("客户入住统计");

        // 创建标题行
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("客户入住统计报表");
        titleCell.setCellStyle(createTitleStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        // 创建日期行
        Row dateRow = sheet.createRow(1);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("统计期间：${startDate} 至 ${endDate}");
        dateCell.setCellStyle(createNormalStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

        // 创建表头
        Row headerRow = sheet.createRow(3);
        String[] headers = {"指标名称", "数值", "", "", "", "", "", ""};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(createHeaderStyle(workbook));
        }

        // 填充数据
        Object[][] data = {
            {"总床位数", "${totalBeds}"},
            {"已入住人数", "${occupiedBeds}"},
            {"空闲床位数", "${availableBeds}"},
            {"床位使用率", "${occupancyRate}%"},
            {"本月新入住人数", "${newCustomers}"},
            {"本月退住人数", "${leftCustomers}"}
        };

        int rowNum = 4;
        for (Object[] rowData : data) {
            Row row = sheet.createRow(rowNum++);
            Cell keyCell = row.createCell(0);
            keyCell.setCellValue((String) rowData[0]);
            keyCell.setCellStyle(createLeftCellStyle(workbook));

            Cell valueCell = row.createCell(1);
            valueCell.setCellValue((String) rowData[1]);
            valueCell.setCellStyle(createRightCellStyle(workbook));
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 1, 7));
        }

        // 护理级别分布小标题
        rowNum++;
        Row levelTitleRow = sheet.createRow(rowNum++);
        Cell levelTitleCell = levelTitleRow.createCell(0);
        levelTitleCell.setCellValue("护理级别分布");
        levelTitleCell.setCellStyle(createSectionStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 7));

        // 护理级别数据
        Object[][] levelData = {
            {"一级护理人数", "${levelOneCare}"},
            {"二级护理人数", "${levelTwoCare}"},
            {"三级护理人数", "${levelThreeCare}"},
            {"四级护理人数", "${levelFourCare}"},
            {"五级护理人数", "${levelFiveCare}"},
            {"六级护理人数", "${levelSixCare}"},
            {"七级护理人数", "${levelSevenCare}"},
            {"八级护理人数", "${levelEightCare}"},
            {"自理人数", "${selfCare}"}
        };

        for (Object[] rowData : levelData) {
            Row row = sheet.createRow(rowNum++);
            Cell keyCell = row.createCell(0);
            keyCell.setCellValue((String) rowData[0]);
            keyCell.setCellStyle(createLeftCellStyle(workbook));

            Cell valueCell = row.createCell(1);
            valueCell.setCellValue((String) rowData[1]);
            valueCell.setCellStyle(createRightCellStyle(workbook));
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 1, 7));
        }

        // 自动调整列宽
        sheet.autoSizeColumn(0);
        sheet.setColumnWidth(1, 20 * 256);

        // 保存文件
        @Cleanup FileOutputStream fos = new FileOutputStream(filePath);
        workbook.write(fos);
        workbook.close();
        
        System.out.println("✅ 客户入住统计模板已生成：" + filePath);
    }

    /**
     * 生成财务统计模板
     */
    public static void createFinanceTemplate(String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("财务统计");

        // 创建标题行
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("财务统计报表");
        titleCell.setCellStyle(createTitleStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        // 创建日期行
        Row dateRow = sheet.createRow(1);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("统计期间：${startDate} 至 ${endDate}");
        dateCell.setCellStyle(createNormalStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 2));

        // 创建表头（3 列）
        Row headerRow = sheet.createRow(3);
        String[] headers = {"收入项目", "金额（元）", "占比"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(createHeaderStyle(workbook));
        }

        // 填充数据行（只设置项目名称，数值留空后期填充）
        String[] items = {
                "总收入",
                "住宿费收入",
                "护理费收入",
                "餐饮费收入",
                "其他收入"
        };

        int rowNum = 4;
        for (String item : items) {
            Row row = sheet.createRow(rowNum++);

            Cell nameCell = row.createCell(0);
            nameCell.setCellValue(item);
            nameCell.setCellStyle(createLeftCellStyle(workbook));

            // 金额单元格（留空，后期填充）
            Cell valueCell = row.createCell(1);
            valueCell.setCellStyle(createRightCellStyle(workbook));

            // 占比单元格（留空，后期填充）
            Cell percentCell = row.createCell(2);
            percentCell.setCellStyle(createPercentStyle(workbook));
        }

        // 欠费和增长信息
        rowNum += 1;
        String[][] extraItems = {
                {"欠费总额"},
                {"欠费客户数"},
                {"环比增长率"}
        };

        for (String[] item : extraItems) {
            Row row = sheet.createRow(rowNum++);

            Cell nameCell = row.createCell(0);
            nameCell.setCellValue(item[0]);
            nameCell.setCellStyle(createLeftCellStyle(workbook));

            Cell valueCell = row.createCell(1);
            valueCell.setCellStyle(createRightCellStyle(workbook));
        }

        // 自动调整列宽
        sheet.autoSizeColumn(0);
        sheet.setColumnWidth(1, 20 * 256);
        sheet.setColumnWidth(2, 15 * 256);

        // 保存文件
        @Cleanup FileOutputStream fos = new FileOutputStream(filePath);
        workbook.write(fos);
        workbook.close();

        System.out.println("✅ 财务统计模板已生成：" + filePath);
    }

    // ========== 样式方法 ==========

    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static CellStyle createSectionStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle createLeftCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle createRightCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
        return style;
    }

    private static CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    /**
     * 主方法 - 运行一次即可生成模板
     */
    public static void main(String[] args) {
        try {
            String basePath = "src/main/resources/templates/";
            createCustomerStatsTemplate(basePath + "customer-stats-template.xlsx");
            createFinanceTemplate(basePath + "finance-template.xlsx");
            System.out.println("🎉 所有模板生成成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
