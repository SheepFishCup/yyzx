package com.cqupt.auth.controller;

import com.cqupt.auth.mapper.UserMapper;
import com.cqupt.pojo.User;
import com.cqupt.utils.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证服务内部 API（供其他微服务通过 OpenFeign 调用）
 */
@Slf4j
@RestController
@RequestMapping("/internal/admin")
public class UserInternalController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/user/{userId}")
    public ResultVo<User> getById(@PathVariable Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null); // 脱敏：不返回密码
        }
        return ResultVo.ok(user);
    }

    @GetMapping("/users/byRole")
    public ResultVo<List<User>> listByRole(@RequestParam Integer roleId) {
        List<User> users = userMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                        .eq("role_id", roleId)
                        .eq("is_deleted", 0)
        );
        users.forEach(u -> u.setPassword(null));
        return ResultVo.ok(users);
    }

    @GetMapping("/user/byEmail")
    public ResultVo<User> getByEmail(@RequestParam String email) {
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                        .eq("email", email)
                        .eq("is_deleted", 0)
        );
        if (user != null) {
            user.setPassword(null);
        }
        return ResultVo.ok(user);
    }
}
