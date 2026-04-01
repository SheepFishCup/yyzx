package com.cqupt.controller.admin;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/20 22:46
 * @description
 */

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController// 表示返回的是json数据
//@RequestMapping("/admin/report")
@RequestMapping("/report")
@Api(tags = "报表管理")
@CrossOrigin
public class ReportController {

}
