package com.cqupt.constant;

/**
 * 内部服务间调用认证常量
 * <p>服务间通过 Feign 调用 /internal/** 端点时，必须携带此 Header 和密钥</p>
 */
public class InternalTokenConstant {

    /** Header 名称 */
    public static final String HEADER_NAME = "X-Internal-Token";

    /** 共享密钥（所有微服务统一配置，生产环境应通过环境变量覆盖） */
    public static final String DEFAULT_TOKEN = "yyzx-internal-secret-2026";

    private InternalTokenConstant() {}
}
