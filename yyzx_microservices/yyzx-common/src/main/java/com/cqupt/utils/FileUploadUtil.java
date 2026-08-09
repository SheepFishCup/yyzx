package com.cqupt.utils;

import com.cqupt.constant.FileUploadConstant;
import com.cqupt.constant.MessageConstant;
import com.cqupt.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件上传工具类
 * <p>上传到 FileUploadConstant.IMAGE_BASE_PATH（默认 ./static/images/）</p>
 * <p>对应 WebConfig 中 file:./static/images/ 的资源映射</p>
 */
@Slf4j
public class FileUploadUtil {

    /**
     * 上传文件（使用默认配置）
     * @param file 上传的文件
     * @return 文件名（WebConfig 映射后通过 /yyzx/images/{fileName} 访问）
     */
    public static String upload(MultipartFile file) throws IOException {
        return upload(file, FileUploadConstant.IMAGE_BASE_PATH,
                FileUploadConstant.DEFAULT_MAX_SIZE,
                FileUploadConstant.DEFAULT_ALLOWED_TYPES);
    }

    /**
     * 上传文件到指定目录
     */
    public static String upload(MultipartFile file, String savePath) throws IOException {
        return upload(file, savePath,
                FileUploadConstant.DEFAULT_MAX_SIZE,
                FileUploadConstant.DEFAULT_ALLOWED_TYPES);
    }

    /**
     * 上传文件（自定义大小和类型）
     */
    public static String upload(MultipartFile file, String savePath,
                                long maxSize, List<String> allowFileTypes) throws IOException {
        return upload(file, savePath, maxSize, allowFileTypes.toArray(new String[0]));
    }

    /**
     * 上传文件（核心方法）
     */
    public static String upload(MultipartFile file, String savePath,
                                long maxSize, String[] allowFileTypes) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new BusinessException("不允许上传空文件");
        }

        if (file.getSize() > maxSize * 1024 * 1024) {
            throw new BusinessException("文件超出大小限制，最大为：" + maxSize + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException("文件格式不正确");
        }

        String extension = originalFilename.substring(
                originalFilename.lastIndexOf(".") + 1).toLowerCase();

        if (allowFileTypes != null && allowFileTypes.length > 0) {
            boolean match = Arrays.stream(allowFileTypes)
                    .anyMatch(type -> type.equalsIgnoreCase(extension));
            if (!match) {
                String allowed = Arrays.stream(allowFileTypes)
                        .collect(Collectors.joining(", "));
                throw new BusinessException(
                        "不支持的文件类型：" + extension + "，支持的类型：" + allowed);
            }
        }

        File destFile = new File(savePath, originalFilename);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
            log.info("创建上传目录：{}", destFile.getParentFile().getAbsolutePath());
        }

        file.transferTo(destFile);
        log.info("文件上传成功：{}", destFile.getAbsolutePath());
        return originalFilename;
    }

    /**
     * 上传图片（使用默认配置）
     */
    public static String uploadImage(MultipartFile file) throws IOException {
        return upload(file, FileUploadConstant.IMAGE_BASE_PATH,
                FileUploadConstant.DEFAULT_MAX_SIZE,
                FileUploadConstant.DEFAULT_ALLOWED_TYPES);
    }

    /**
     * 删除文件
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            boolean deleted = file.delete();
            log.info("文件{}：{}", deleted ? "删除成功" : "删除失败", filePath);
            return deleted;
        }
        log.warn("文件不存在：{}", filePath);
        return false;
    }

    /**
     * 获取文件完整路径
     */
    public static String getFullPath(String fileName) {
        return FileUploadConstant.IMAGE_BASE_PATH + fileName;
    }
}
