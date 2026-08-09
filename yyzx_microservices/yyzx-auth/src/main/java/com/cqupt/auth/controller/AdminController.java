package com.cqupt.auth.controller;
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
import com.cqupt.auth.service.UserService;
import com.cqupt.utils.HybridBlacklistUtils;
import com.cqupt.utils.ImageCodeUtil;
import com.cqupt.utils.ResultVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@RestController// 表示返回的是json数据
//@RequestMapping("/admin/admin")
@RequestMapping("/admin")
@Tag(name = "用户管理")
@CrossOrigin
public class AdminController {
    @Autowired
    private UserService userService;
    @Autowired
    private HybridBlacklistUtils blacklistUtils;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private static final ConcurrentHashMap<String, CacheEntry> LOCAL_CACHE = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRE_MILLIS = 300000;

    private static class CacheEntry {
        Object value;
        long expireTime;

        CacheEntry(Object value, long ttlMillis) {
            this.value = value;
            this.expireTime = System.currentTimeMillis() + ttlMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    private void putToLocalCache(String key, Object value, long ttlMillis) {
        LOCAL_CACHE.put(key, new CacheEntry(value, ttlMillis));
    }

    @GetMapping("/generate")
    @Operation(summary = "生成图片验证码")
    public ResultVo<ImageCodeDTO> generateCaptcha() {
        try {
            ImageCodeDTO imageCodeDTO = ImageCodeUtil.generateCode();
            String key = RedisConstant.IMAGE_CODE_PREFIX + imageCodeDTO.getUuid();
            String code = imageCodeDTO.getCode();
            log.info("生成验证码 - UUID: {}, 验证码文本：{}", imageCodeDTO.getUuid(), code);

            try {
                redisTemplate.opsForValue().set(key, code,
                        Duration.ofSeconds(RedisConstant.IMAGE_CODE_EXPIRE));
                String savedValue = (String) redisTemplate.opsForValue().get(key);
                log.info("Redis 存储验证 - Key: {}, 读取结果：{}", key, savedValue);
            } catch (RedisConnectionFailureException e) {
                log.error("Redis 连接失败，使用本地缓存存储验证码", e);
            }

            putToLocalCache(key, code, RedisConstant.IMAGE_CODE_EXPIRE * 1000L);

            return ResultVo.ok(imageCodeDTO);
        } catch (Exception e) {
            log.error("生成验证码失败", e);
            return ResultVo.fail("验证码生成失败");
        }
    }

    @PostMapping("/loginWithCaptcha")
    @Operation(summary = "带验证码的登录")
    public ResultVo loginWithCaptcha(@Valid @RequestBody LoginWithCodeDTO loginDTO) {
        log.info("带验证码登录请求：{}", loginDTO.getUsername());
        return userService.loginWithCaptcha(loginDTO);
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

    @Operation(summary = "忘记密码 - 发送重置邮件")
    @PostMapping("/forgotPassword")
    public ResultVo forgotPassword(@Valid @RequestBody ForgotPasswordDTO forgotDTO) {
        log.info("忘记密码请求，邮箱：{}", forgotDTO.getEmail());
        return userService.forgotPassword(forgotDTO);
    }

    @Operation(summary = "忘记密码 - 重置密码")
    @PostMapping("/resetPassword")
    public ResultVo resetPassword(@Valid @RequestBody ResetPasswordDTO resetDTO) {
        log.info("重置密码请求");
        return userService.resetPassword(resetDTO);
    }

    @Operation(summary = "校验重置令牌")
    @GetMapping("/verifyResetToken")
    public ResultVo verifyResetToken(@RequestParam String token) {
        return userService.verifyResetToken(token);
    }

    @PostMapping("/unblock")
    public ResultVo unblockUser(@RequestParam String username) {
        try {
            boolean removed = blacklistUtils.removeFromBlacklist(username);

            if (removed) {
                log.info("✅ 已成功解封用户：{}", username);
                return ResultVo.ok("解封成功");
            } else {
                log.warn("⚠️ 用户 {} 不在黑名单中", username);
                return ResultVo.fail("该用户不在黑名单中");
            }
        } catch (Exception e) {
            log.error("❌ 解封用户失败：{}", username, e);
            return ResultVo.fail("解封失败：" + e.getMessage());
        }
    }
}
