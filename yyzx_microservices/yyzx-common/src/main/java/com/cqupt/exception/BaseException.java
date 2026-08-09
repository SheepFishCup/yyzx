package com.cqupt.exception;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/02/28 17:07
 * @description 自定义异常类
 */

public class BaseException extends RuntimeException{
    public BaseException() {
    }
    public BaseException(String message) {
        super(message);
    }
}
