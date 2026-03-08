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

}
