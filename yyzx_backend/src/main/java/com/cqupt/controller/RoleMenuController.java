package com.cqupt.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.cqupt.pojo.RoleMenu;
import com.cqupt.service.RoleMenuService;
import com.cqupt.utils.ResultVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/07/01 09:04
 * @description
 */

@RestController
@RequestMapping("/rolemenu")
@Api(tags = "角色菜单管理") // swagger分组
@CrossOrigin // 解决跨域问题
public class RoleMenuController {
    @Autowired
    private RoleMenuService roleMenuService;

    @GetMapping("/listRoleMenu")
    @ApiOperation("查询角色菜单")
    public ResultVo listRoleMenu() throws Exception {
        roleMenuService.list();
        return ResultVo.ok("查询成功");
    }

    @PostMapping("/addRoleMenu")
    @ApiOperation("添加角色菜单")
    public ResultVo addRoleMenu(RoleMenu roleMenu) throws Exception {
        roleMenuService.save(roleMenu);
        return ResultVo.ok("添加成功");
    }

    @PostMapping("/updateRoleMenu")
    @ApiOperation("更新角色菜单")
    public ResultVo updateRoleMenu(RoleMenu roleMenu) throws Exception {
        roleMenuService.updateById(roleMenu);
        return ResultVo.ok("更新成功");
    }

    @GetMapping("/removeRoleMenu")
    @ApiOperation("删除角色菜单")
    public ResultVo removeRoleMenu(Integer id) throws Exception {
        roleMenuService.removeById(id);
        return ResultVo.ok("删除成功");
    }
}
