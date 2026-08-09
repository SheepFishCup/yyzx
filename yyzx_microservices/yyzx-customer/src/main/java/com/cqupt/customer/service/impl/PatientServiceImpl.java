package com.cqupt.customer.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/08 19:16
 * @description
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.constant.MessageConstant;
import com.cqupt.constant.WeChatConstant;
import com.cqupt.exception.LoginFailedException;
import com.cqupt.customer.mapper.PatientMapper;
import com.cqupt.pojo.Patient;
import com.cqupt.properties.JwtProperties;
import com.cqupt.properties.WeChatProperties;
import com.cqupt.customer.service.PatientService;
import com.cqupt.utils.HttpClientUtil;
import com.cqupt.utils.JwtUtil;
import com.cqupt.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {
    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private PatientMapper patientMapper;
    @Override
    public ResultVo<Patient> login(String code) {
        Map<String, String> queryParams = new HashMap<>();//请求参数
        queryParams.put(WeChatConstant.PARAM_APPID, weChatProperties.getAppid());//微信开放平台appid
        queryParams.put(WeChatConstant.PARAM_SECRET, weChatProperties.getSecret());//微信开放平台appsecret
        queryParams.put(WeChatConstant.PARAM_JS_CODE, code);//微信开放平台appid
        queryParams.put(WeChatConstant.PARAM_GRANT_TYPE, WeChatConstant.GRANT_TYPE_AUTHORIZATION_CODE);//授权类型
        String response = HttpClientUtil.doGet(WeChatConstant.WECHAT_SERVER_LOGIN_URL, queryParams);//发送请求

        JSONObject jsonObject = JSON.parseObject(response);//解析响应结果
        String openid = jsonObject.getString("openid");//获取openid
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);//登录失败
        }

        // 查找数据库中是否存在对应openid的用户
        Patient patient = patientMapper.getPatientByOpenid(openid);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", patient.getId());
        claims.put("openid", openid);
        String token = JwtUtil.createToken(
                jwtProperties.getUserSecret(),
                jwtProperties.getUserExpire(),
                claims);

        return ResultVo.ok(patient, token);
    }
}
