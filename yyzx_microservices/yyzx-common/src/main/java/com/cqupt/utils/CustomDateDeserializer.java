package com.cqupt.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * 自定义日期反序列化器，用于将JSON中的日期字符串转换为Date对象。
 *
 * @param dateConverter 日期转换器，用于实际的日期转换操作
 */
@Component
public class CustomDateDeserializer extends JsonDeserializer<Date> {

    private final DateConverter dateConverter;

    /**
     * 构造函数，初始化日期转换器。
     *
     * @param dateConverter 日期转换器实例
     */
    @Autowired
    public CustomDateDeserializer(DateConverter dateConverter) {
        this.dateConverter = dateConverter;
    }

    /**
     * 将JSON解析器中的日期字符串反序列化为Date对象。
     *
     * @param p JSON解析器
     * @param ctxt 反序列化上下文
     * @return 解析得到的Date对象
     * @throws IOException 如果输入无效或解析失败
     */
    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText();
        return dateConverter.convert(dateStr);
    }
}