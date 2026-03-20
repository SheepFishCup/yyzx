package com.cqupt.controller.admin;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/07/01 10:23
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.constant.RedisConstant;
import com.cqupt.dto.*;
import com.cqupt.pojo.User;
import com.cqupt.service.UserService;
import com.cqupt.utils.ImageCodeUtil;
import com.cqupt.utils.ResultVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;


@Slf4j
@RestController// 表示返回的是json数据
//@RequestMapping("/admin/admin")
@RequestMapping("/admin")
@Api(tags = "用户管理") // swagger分组
@CrossOrigin
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/generate")
    @ApiOperation("生成图片验证码")
    public ResultVo<ImageCodeDTO> generateCaptcha() {
        try {
            // 生成验证码
            ImageCodeDTO imageCodeDTO = ImageCodeUtil.generateCode();
            // 将验证码存入 Redis（5 分钟有效期）
            String key = RedisConstant.IMAGE_CODE_PREFIX + imageCodeDTO.getUuid();
            String code = imageCodeDTO.getCode();
            log.info("生成验证码 - UUID: {}, 验证码文本：{}", imageCodeDTO.getUuid(), code);
            redisTemplate.opsForValue().set(key, code,
                    Duration.ofSeconds(RedisConstant.IMAGE_CODE_EXPIRE));
            // 验证是否存入成功
            String savedValue = (String) redisTemplate.opsForValue().get(key);
            log.info("Redis 存储验证 - Key: {}, 读取结果：{}", key, savedValue);
            // 清除验证码文本（生产环境）
            // imageCodeDTO.setCode(null);
            return ResultVo.ok(imageCodeDTO);
        } catch (Exception e) {
            log.error("生成验证码失败", e);
            return ResultVo.fail("验证码生成失败");
        }
    }

    @PostMapping("/loginWithCaptcha")
    @ApiOperation("带验证码的登录")
    public ResultVo loginWithCaptcha(@Valid @RequestBody LoginWithCodeDTO loginDTO) {
        log.info("带验证码登录请求：{}", loginDTO.getUsername());
        return userService.loginWithCaptcha(loginDTO);
    }

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
    //更改密码
    @PostMapping("/updatePassword")
    public ResultVo updatePassword(String username, String oldPassword, String newPassword) throws Exception {
        log.info("用户修改密码：{}", username);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        Long id = userService.getOne(queryWrapper).getId();
        return userService.changePassword(id, oldPassword, newPassword);
    }

    @ApiOperation("忘记密码 - 发送重置邮件")
    @PostMapping("/forgotPassword")
    public ResultVo forgotPassword(@Valid @RequestBody ForgotPasswordDTO forgotDTO) {
        log.info("忘记密码请求，邮箱：{}", forgotDTO.getEmail());
        return userService.forgotPassword(forgotDTO);
    }

    @ApiOperation("忘记密码 - 重置密码")
    @PostMapping("/resetPassword")
    public ResultVo resetPassword(@Valid @RequestBody ResetPasswordDTO resetDTO) {
        log.info("重置密码请求");
        return userService.resetPassword(resetDTO);
    }

    @ApiOperation("校验重置令牌")
    @GetMapping("/verifyResetToken")
    public ResultVo verifyResetToken(@RequestParam String token) {
        return userService.verifyResetToken(token);
    }
}
