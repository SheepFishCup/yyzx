package com.cqupt.utils;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/19 16:23
 * @description 日期转换格式
 */

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class DateConverter implements Converter<String, Date> {
    // 日期格式
    private static final List<String> format = new ArrayList<>();
    static {
        format.add("yyyy-MM");
        format.add("yyyy-MM-dd");
        format.add("yyyy-MM-dd HH:mm");
        format.add("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public Date convert(String source) {
        // trim(): 去掉字符串首尾空格
        String value = source.trim();
        if ("".equals(value)) {
            return null;
        }
        // ^ 表示开头  $ 表示结尾
        // \ 转义字符  \d 表示数字
        // {4} 重复4次 {1,2} 表示重复1到2次
        // 表示 4位数字-1到2位数字 (01,02)
        if (source.matches("^\\d{4}-\\d{1,2}$")){
            return parseDate(source, format.get(0));
        } else if (source.matches("^\\d{4}-\\d{1,2}-\\d{1,2}$")){
            return parseDate(source, format.get(1));
            //空格{1} 表示有一个空格
        } else if (source.matches("^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}$")){
            return parseDate(source, format.get(2));
        } else if (source.matches("^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}:\\d{1,2}$")){
            return parseDate(source, format.get(3));
        }else {
            throw new IllegalArgumentException("日期格式异常：需要xxxx-xx-xx:'" + source + "'");
        }

    }

    public Date parseDate(String dateStr, String format) {
        Date date=null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            date = dateFormat.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }
}
