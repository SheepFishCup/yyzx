package com.cqupt.feign;

import com.cqupt.pojo.User;
import com.cqupt.utils.ResultVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证服务 Feign 客户端
 * <p>供 notification、customer 等模块查询用户信息</p>
 */
@FeignClient(name = "yyzx-auth", contextId = "userFeignClient", path = "/yyzx/internal/admin",
        fallbackFactory = UserFeignClientFallbackFactory.class)
public interface UserFeignClient {

    /** 根据 ID 获取用户 */
    @GetMapping("/user/{userId}")
    ResultVo<User> getById(@PathVariable("userId") Long userId);

    /** 根据角色获取用户列表（如查所有管理员） */
    @GetMapping("/users/byRole")
    ResultVo<List<User>> listByRole(@RequestParam("roleId") Integer roleId);

    /** 根据邮箱查询用户 */
    @GetMapping("/user/byEmail")
    ResultVo<User> getByEmail(@RequestParam("email") String email);
}
