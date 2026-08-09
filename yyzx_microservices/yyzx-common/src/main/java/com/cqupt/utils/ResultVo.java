package com.cqupt.utils;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/19 15:07
 * @description 结果视图,将实体类映射为json格式给前端
 */


import lombok.Data;

@Data
public class ResultVo<T> {
    private boolean flag;
    private String message;
    private T data;

    // 成功添加 message
    public static ResultVo ok(String message){
        ResultVo resultVo = new ResultVo();
        resultVo.setMessage(message);
        resultVo.setFlag(true);
        return resultVo;
    }
    // 成功添加data
    public static <T> ResultVo ok(T data){
        ResultVo resultVo = new ResultVo();
        resultVo.setData(data);
        resultVo.setFlag(true);
        return resultVo;
    }
    // 成功添加message和data
    public static <T> ResultVo ok(String message, T data){
        ResultVo resultVo = new ResultVo();
        resultVo.setMessage(message);
        resultVo.setData(data);
        resultVo.setFlag(true);
        return resultVo;
    }

    public static <T> ResultVo ok(T data, String message){
        ResultVo resultVo =  new ResultVo();
        resultVo.setData(data);
        resultVo.setMessage(message);
        resultVo.setFlag(true);
        return resultVo;
    }

    // 失败添加message
    public static ResultVo fail(String message){
        ResultVo resultVo = new ResultVo();
        resultVo.setMessage(message);
        resultVo.setFlag(false);
        return resultVo;
    }
    // 失败添加data
    public static <T> ResultVo fail(T data){
        ResultVo resultVo = new ResultVo();
        resultVo.setData(data);
        resultVo.setFlag(false);
        return resultVo;
    }
    // 失败添加data和message
    public static <T> ResultVo fail(T data, String message) {
        ResultVo<T> resultVo = new ResultVo<>();
        resultVo.setData(data);
        resultVo.setMessage(message);
        resultVo.setFlag(false);
        return resultVo;
    }

    // 异常
    public static ResultVo error(Exception e){
        ResultVo resultVo = new ResultVo();
        resultVo.setMessage("系统发送了异常:"+e.getMessage());
        resultVo.setFlag(false);
        return resultVo;
    }

    public static ResultVo error(String message) {
        ResultVo resultVo = new ResultVo();
        resultVo.setMessage("系统发送了异常:"+message);
        resultVo.setFlag(false);
        return resultVo;
    }
}
