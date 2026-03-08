package com.cqupt.controller.admin;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/07/01 10:23
 * @description
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.dto.UserDTO;
import com.cqupt.pojo.User;
import com.cqupt.service.UserService;
import com.cqupt.utils.ResultVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController// 表示返回的是json数据
//@RequestMapping("/admin/user")
@RequestMapping("/user")
@Api(tags = "用户管理") // swagger分组
@CrossOrigin
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @ApiOperation("用户登录接口")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "String", name = "username", value = "用户登录账号", required = true),
            @ApiImplicitParam(dataType = "String", name = "password", value = "用户登录密码", required = true)
    })
    public ResultVo<User> login(String username, String password) throws Exception {
        log.info("用户登录接口，账号：{} 密码：{}", username, password);
        return userService.login(username, password);
    }
    @GetMapping("/findUserPage")
    public ResultVo<Page<User>> findUserPage(UserDTO userDTO) throws Exception {
        log.info("查询用户分页：{}", userDTO);
        return userService.findUserPage(userDTO);
    }
    @GetMapping("/findAllUserPage")
    public ResultVo<Page<User>> findAllUserPage(UserDTO userDTO) throws Exception {
        log.info("查询所有用户分页：{}", userDTO);
        return userService.findAllUserPage(userDTO);
    }

//    @CachePut(cacheNames = "userCache", key = "#user.id")
    @PostMapping("/addUser")
    public ResultVo addUser(User user) throws Exception {
        log.info("添加用户：{}", user);
        return userService.addUser(user);
    }


    @PostMapping("/updateUser")
    public ResultVo updateUser(User user) throws Exception {
        log.info("更新用户：{}", user);
        return userService.updateUser(user);
    }
    @GetMapping("/delUser")
    public ResultVo deleteUser(Long id) throws Exception {
        log.info("删除用户：{}", id);
        return userService.deleteUser(id);
    }
}
