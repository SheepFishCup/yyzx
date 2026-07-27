package com.cqupt.utils;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/05 15:45
 * @description
 */


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqupt.constant.MessageConstant;
import com.cqupt.exception.BusinessException;
import com.cqupt.mapper.FoodMapper;
import com.cqupt.pojo.Food;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文件上传工具类
 */
@Slf4j
public class FileUploadUtil {
    // 默认允许的文件类型
    private static final List<String> DEFAULT_ALLOWED_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif");

    // 默认最大文件大小 (MB)
    private static final long DEFAULT_MAX_SIZE = 10L;

    // 默认图片保存路径
    private static final String IMAGE_BASE_PATH = "D:/BaiduNetdiskDownload/软件项目/yyzx/yyzx_backend/src/main/resources/static/images/";

    /**
     * 上传文件到指定目录（使用默认配置）
     * @param file 上传的文件
     * @return 文件访问路径（相对路径，可用于 URL 访问）
     * @throws IOException IO 异常
     */
    public static String upload(MultipartFile file) throws IOException {
        return upload(file, IMAGE_BASE_PATH, DEFAULT_MAX_SIZE,
                DEFAULT_ALLOWED_TYPES.toArray(new String[0]));
    }

    /**
     * 上传文件到指定目录
     * @param file 上传的文件
     * @param savePath 文件保存路径
     * @return 文件访问路径
     * @throws IOException IO 异常
     */
    public static String upload(MultipartFile file, String savePath) throws IOException {
        return upload(file, savePath, DEFAULT_MAX_SIZE, DEFAULT_ALLOWED_TYPES);
    }

    /**
     * 上传文件（可自定义文件大小和类型限制）
     * @param file 上传的文件
     * @param savePath 文件保存路径
     * @param maxSize 最大文件大小 (MB)
     * @param allowFileTypes 允许的文件类型列表
     * @return 文件访问路径（不含前缀）
     * @throws IOException IO 异常
     * @throws RuntimeException 文件验证失败抛出
     */
    public static String upload(MultipartFile file, String savePath,
                                long maxSize, List<String> allowFileTypes) throws IOException {
        return upload(file, savePath, maxSize, allowFileTypes.toArray(new String[0]));
    }

    /**
     * 上传文件（可自定义文件大小和类型限制）
     * @param file 上传的文件
     * @param savePath 文件保存路径
     * @param maxSize 最大文件大小 (MB)
     * @param allowFileTypes 允许的文件类型数组
     * @return 文件访问路径（不含前缀）
     * @throws IOException IO 异常
     * @throws RuntimeException 文件验证失败抛出
     */
    public static String upload(MultipartFile file, String savePath,
                                long maxSize, String[] allowFileTypes) throws IOException {

        // 校验文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("不允许上传空文件");
        }

        // 校验文件大小（与 FileValidator 逻辑一致）
        if (file.getSize() > maxSize * 1024 * 1024) {
            throw new BusinessException("文件超出大小限制，最大为：" + maxSize + "MB");
        }

        // 获取原始文件名和扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException("文件格式不正确");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

        // 校验文件类型（与 FileValidator 逻辑一致）
        if (allowFileTypes != null && allowFileTypes.length > 0) {
            boolean match = Arrays.stream(allowFileTypes)
                    .anyMatch(type -> type.equalsIgnoreCase(extension));
            if (!match) {
                String allowedTypesStr = Arrays.stream(allowFileTypes)
                        .collect(Collectors.joining(", "));
                throw new BusinessException("不支持的文件类型：" + extension +
                        "，支持的类型：" + allowedTypesStr);
            }
        }

        String fileName = UUID.randomUUID().toString() + "." + extension;

        // 创建目标文件
        File destFile = new File(savePath, fileName);

        // 确保目录存在
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
            log.info("创建目录：{}", destFile.getParentFile().getAbsolutePath());
        }

        // 保存文件
        try {
            file.transferTo(destFile);
            log.info("文件上传成功：{}", destFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("文件保存失败：{}", destFile.getAbsolutePath(), e);
            throw new IOException("文件保存失败：" + e.getMessage());
        }

        return fileName;
    }

    /**
     * 上传图片文件（使用 @File 注解的默认配置）
     * @param file 上传的文件
     * @return 文件访问路径（相对路径，可用于 URL 访问）
     * @throws IOException IO 异常
     */
    public static String uploadImage(MultipartFile file) throws IOException {
        return upload(file, IMAGE_BASE_PATH);
    }

    /**
     * 删除文件
     * @param filePath 文件完整路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            boolean deleted = file.delete();
            if (deleted) {
                log.info("文件删除成功：{}", filePath);
            } else {
                log.warn("文件删除失败：{}", filePath);
            }
            return deleted;
        }
        log.warn("文件不存在：{}", filePath);
        return false;
    }

    /**
     * 获取文件完整存储路径
     * @param fileName 文件名
     * @return 完整路径
     */
    public static String getFullPath(String fileName) {
        return IMAGE_BASE_PATH + fileName;
    }

}
