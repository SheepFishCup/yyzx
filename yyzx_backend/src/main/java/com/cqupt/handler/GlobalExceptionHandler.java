package com.cqupt.handler;

/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/19 15:02
 * @description 全局异常处理器
 */

import com.cqupt.utils.ResultVo;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.jsonwebtoken.SignatureException;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice
public class GlobalExceptionHandler {
    // 定义异常处理的方法
    // SignatureException    token的非法异常

    @ExceptionHandler(SignatureException.class)
    @ResponseBody
    public ResultVo SignatureExceptionhandle(SignatureException e) {
        return ResultVo.fail("token的非法异常","token_error");
    }

    // MalformedJwtException   token的解析异常
    @ExceptionHandler(MalformedJwtException.class)
    @ResponseBody
    public ResultVo MalformedJwtExceptionhandle(MalformedJwtException e) {
        return ResultVo.fail("token的解析异常","token_error");
    }

    // ExpiredJwtException  token的过期异常
    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseBody
    public ResultVo ExpiredJwtExceptionhandle(ExpiredJwtException e) {
        return ResultVo.fail("登录超时,请重新登录","token_error");
    }

    //统一处理其它异常
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResultVo Exceptionhandle(Exception e) {
        if (e.getMessage().contains("token")){
            return ResultVo.fail(e.getMessage(),"token_error");
        }
        return ResultVo.fail( e.getMessage());
    }

}
