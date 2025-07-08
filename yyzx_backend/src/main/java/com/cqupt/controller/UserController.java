package com.cqupt.controller;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Api(tags = "用户管理") // swagger分组
@CrossOrigin
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    @ApiOperation("用户登录接口")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "String", name = "username", value = "用户登录账号", required = true),
            @ApiImplicitParam(dataType = "String", name = "password", value = "用户登录密码", required = true)
    })
    public ResultVo<User> login(String username, String password) throws Exception {
        return userService.login(username, password);
    }
    @GetMapping("/findUserPage")
    public ResultVo<Page<User>> findUserPage(UserDTO userDTO) throws Exception {
        return userService.findUserPage(userDTO);
    }
    @GetMapping("/findAllUserPage")
    public ResultVo<Page<User>> findAllUserPage(UserDTO userDTO) throws Exception {
        return userService.findAllUserPage(userDTO);
    }
    @PostMapping("/addUser")
    public ResultVo addUser(User user) throws Exception {
        return userService.addUser(user);
    }
    @PostMapping("/updateUser")
    public ResultVo updateUser(User user) throws Exception {
        return userService.updateUser(user);
    }
    @GetMapping("/delUser")
    public ResultVo deleteUser(Integer id) throws Exception {
        return userService.deleteUser(id);
    }
}
