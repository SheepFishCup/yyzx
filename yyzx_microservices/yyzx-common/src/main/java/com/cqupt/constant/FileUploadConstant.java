package com.cqupt.constant;

/**
 * 文件上传常量
 * <p>存储路径为相对路径，相对于各服务 JVM 工作目录</p>
 * <p>生产环境通过 volume 挂载到容器内固定路径</p>
 */
public class FileUploadConstant {

    /** 图片存储路径（相对于 JVM user.dir） */
    public static final String IMAGE_BASE_PATH = "./static/images/";

    /** 文件访问 URL 前缀（网关代理后相对路径即可） */
    public static final String FILE_ACCESS_PATH = "/yyzx/images/";

    /** 默认允许的文件类型 */
    public static final String[] DEFAULT_ALLOWED_TYPES = {"jpg", "jpeg", "png", "gif"};

    /** 默认最大文件大小（MB） */
    public static final long DEFAULT_MAX_SIZE = 10L;

    private FileUploadConstant() {}
}
