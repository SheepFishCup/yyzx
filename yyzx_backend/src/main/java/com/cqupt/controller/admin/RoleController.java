package com.cqupt.controller.admin;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/07/01 08:55
 * @description
 */

import com.cqupt.pojo.Role;
import com.cqupt.service.RoleService;
import com.cqupt.utils.ResultVo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
//@RequestMapping("/admin/role")
@RequestMapping("/role")
@Api(tags = "角色管理") // swagger分组
@CrossOrigin // 解决跨域问题
public class RoleController {
    @Autowired
    private RoleService roleService;

    @PostMapping("/addRole")
    public ResultVo addRole(Role role) throws Exception {
        log.info("添加角色：{}", role);
        roleService.save(role);
        return ResultVo.ok("添加成功");
    }

    @PostMapping("/updateRole")
    public ResultVo updateRole(Role role) throws Exception {
        log.info("修改角色：{}", role);
        roleService.updateById(role);
        return ResultVo.ok("修改成功");
    }

    @GetMapping("/removeRole")
    public ResultVo removeRole(Integer id) throws Exception {
        log.info("删除角色：{}", id);
        roleService.removeById(id);
        return ResultVo.ok("删除成功");
    }

    @GetMapping("/listRole")
    public ResultVo listRole() throws Exception {
        roleService.list();
        return ResultVo.ok("查询成功");
    }
}
