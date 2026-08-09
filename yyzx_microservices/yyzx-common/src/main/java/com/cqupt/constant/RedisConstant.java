package com.cqupt.constant;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/06 12:52
 * @description
 */

public class RedisConstant {
    // 用户添加锁前缀
    public static final String USER_ADD_LOCK_PREFIX = "lock:user:add:";
    // Token 缓存前缀
    public static final String USER_TOKEN_PREFIX = "user:token:";
    // Token 过期时间（秒）- 与 JWT 过期时间保持一致
    public static final long TOKEN_EXPIRE_SECONDS = 86400; // 24 小时
    // 用户信息缓存前缀
    public static final String USER_INFO_PREFIX = "user:info:";
    // 登录错误次数前缀
    public static final String LOGIN_ERROR_PREFIX = "login:error:";
    // 登录错误次数限制
    public static final int LOGIN_ERROR_LIMIT = 5;
    // 登录错误锁定时间（小时）
    public static final int LOGIN_ERROR_LOCK_MINUTES = 5;
    // 用户缓存时间（小时）
    public static final int USER_CACHE_HOURS = 24;

    // 图片验证码相关
    public static final String IMAGE_CODE_PREFIX = "image:code:";       // 图片验证码前缀
    public static final long IMAGE_CODE_EXPIRE = 300;                    // 图片验证码有效期（5分钟）
    // 密码重置相关
    public static final String PASSWORD_RESET_PREFIX = "pwd:reset:";     // 密码重置令牌前缀
    public static final long PASSWORD_RESET_EXPIRE = 1800;               // 重置链接有效期（30分钟）
}
