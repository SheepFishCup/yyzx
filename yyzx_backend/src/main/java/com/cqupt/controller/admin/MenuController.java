package com.cqupt.controller.admin;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/07/01 09:01
 * @description
 */

import com.cqupt.pojo.Menu;
import com.cqupt.service.MenuService;
import com.cqupt.utils.ResultVo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
//@RequestMapping("/admin/menu")
@RequestMapping("/menu")
@Api(tags = "菜单管理") // swagger分组
@CrossOrigin // 解决跨域问题
public class MenuController {
    @Autowired
    private MenuService menuService;

    @GetMapping("/listMenu")
    public ResultVo listMenu() {
        menuService.list();
        return ResultVo.ok("查询成功");
    }

    @PostMapping("/addMenu")
    public ResultVo addMenu(Menu menu) {
        log.info("添加菜单：{}", menu);
        menuService.save(menu);
        return ResultVo.ok("添加成功");
    }

    @PostMapping("/updateMenu")
    public ResultVo updateMenu(Menu menu) {
        log.info("更新菜单：{}", menu);
        menuService.updateById(menu);
        return ResultVo.ok("更新成功");
    }

    @GetMapping("/removeMenu")
    public ResultVo removeMenu(Integer id) {
        log.info("删除菜单,参数为：{}", id);
        menuService.removeById(id);
        return ResultVo.ok("删除成功");
    }
}
