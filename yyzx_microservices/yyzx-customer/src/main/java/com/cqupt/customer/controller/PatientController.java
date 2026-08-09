package com.cqupt.customer.controller;

import com.cqupt.pojo.Patient;
import com.cqupt.customer.service.PatientService;
import com.cqupt.utils.ResultVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/08 19:04
 * @description
 */
@Slf4j
@RestController// 表示返回的是json数据
//@RequestMapping("/user/patient")
@RequestMapping("/patient")
@Api(tags = "用户管理") // swagger分组
@CrossOrigin
public class PatientController {
    @Autowired
    private PatientService patientService;
    @PostMapping("/login")
    @ApiOperation("用户登录接口")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "String", name = "username", value = "用户登录账号", required = true),
            @ApiImplicitParam(dataType = "String", name = "password", value = "用户登录密码", required = true)
    })
    public ResultVo<Patient> login(String code) throws Exception {
        return patientService.login(code);
    }
}
