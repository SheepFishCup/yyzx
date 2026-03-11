package com.cqupt.utils;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/10 12:08
 * @description
 */

import com.cqupt.dto.ImageCodeDTO;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

/**
 * 图片验证码工具类
 */
public class ImageCodeUtil {

    private static Producer producer;

    static {
        // 配置Kaptcha
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border", "no");
        properties.setProperty("kaptcha.border.color", "105,179,90");
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        properties.setProperty("kaptcha.image.width", "120");
        properties.setProperty("kaptcha.image.height", "40");
        properties.setProperty("kaptcha.textproducer.font.size", "35");
        properties.setProperty("kaptcha.session.key", "code");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        properties.setProperty("kaptcha.textproducer.font.names", "Arial");
        properties.setProperty("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.WaterRipple");
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");

        Config config = new Config(properties);
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(config);

        producer = defaultKaptcha;
    }

    /**
     * 生成验证码图片并返回base64
     */
    public static ImageCodeDTO generateCode() throws IOException {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String code = producer.createText();

        BufferedImage image = producer.createImage(code);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);

        // 转换为base64
        BASE64Encoder encoder = new BASE64Encoder();
        String base64 = "data:image/jpeg;base64," + encoder.encode(outputStream.toByteArray());

        ImageCodeDTO imageCodeDTO = new ImageCodeDTO();
        imageCodeDTO.setUuid(uuid);
        imageCodeDTO.setBase64(base64);
        imageCodeDTO.setCode(code);
        return imageCodeDTO;
    }
}