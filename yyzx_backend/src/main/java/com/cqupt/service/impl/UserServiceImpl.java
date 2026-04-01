package com.cqupt.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/07/01 10:06
 * @description
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.constant.*;
import com.cqupt.context.BaseContext;
import com.cqupt.dto.ForgotPasswordDTO;
import com.cqupt.dto.LoginWithCodeDTO;
import com.cqupt.dto.ResetPasswordDTO;
import com.cqupt.dto.UserDTO;
import com.cqupt.exception.AccountLockedException;
import com.cqupt.exception.AccountNotFoundException;
import com.cqupt.exception.BusinessException;
import com.cqupt.exception.PasswordErrorException;
import com.cqupt.mapper.MenuMapper;
import com.cqupt.mapper.RoleMenuMapper;
import com.cqupt.mapper.UserMapper;
import com.cqupt.pojo.Menu;
import com.cqupt.pojo.User;
import com.cqupt.properties.JwtProperties;
import com.cqupt.service.MailService;
import com.cqupt.service.UserService;
import com.cqupt.utils.JwtUtil;
import com.cqupt.utils.ResultVo;
import com.cqupt.websocket.WebSocketServer;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MenuMapper menuMapper;
    @Autowired
    private RoleMenuMapper roleMenuMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private MailService mailService;
    @Autowired
    private WebSocketServer webSocketServer;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    // 记录登录错误
    private void recordLoginError(String username) {
        String key = RedisConstant.LOGIN_ERROR_PREFIX + username;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofHours(1)); // 1小时内有效
        }

        // 超过5次，锁定账号
        if (count >= 5) {
            lockAccount(username);
        }
    }
    // 锁定账号
    private void lockAccount(String username) {
        UpdateWrapper<User> uw = new UpdateWrapper<>();
        uw.eq("username", username);
        uw.set("status", StatusConstant.DISABLE);
        userMapper.update(null, uw);
        // 清除缓存
        String userKey = RedisConstant.USER_INFO_PREFIX + username;
        redisTemplate.delete(userKey);
    }
    @Override
    public ResultVo<Page<User>> findUserPage(UserDTO userDTO) throws Exception {
        // 检查当前用户是否有权限查看
        Long currentId = BaseContext.getCurrentId();
        User currentUser = userMapper.selectById(currentId);
        if (currentUser.getRoleId() != 1) { // 假设 1 是管理员
            throw new AccessDeniedException("无权访问");
        }
        Integer current=userDTO.getCurrent()!=null?userDTO.getCurrent():1;
        Integer pageSize=userDTO.getPageSize()!=null?userDTO.getPageSize():6;
        // 参数范围校验，防止恶意请求
        if (current < 1) {
            current = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        Page<User> myPage = new Page<>(current,pageSize);
        // 使用 Mapper 查询（更直接、更可控）
        userMapper.selectUserPage(myPage, userDTO.getNickName(), userDTO.getRoleId());

        return ResultVo.ok(myPage);
    }

    @Override
    public ResultVo<Page<User>> findAllUserPage(UserDTO userDTO) throws Exception {
        // 检查当前用户是否有权限查看
        Long currentId = BaseContext.getCurrentId();
        User currentUser = userMapper.selectById(currentId);
        if (currentUser.getRoleId() != 1) { // 假设 1 是管理员
            throw new AccessDeniedException("无权访问");
        }
        Integer current=userDTO.getCurrent()!=null?userDTO.getCurrent():1;
        Integer pageSize=userDTO.getPageSize()!=null?userDTO.getPageSize():6;
        // 参数范围校验，防止恶意请求
        if (current < 1) {
            current = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        Page<User> myPage = new Page<>(current,pageSize);
        userMapper.selectUserPage(myPage, userDTO.getNickName(), userDTO.getRoleId());
        return ResultVo.ok(myPage);
    }

    @Override
    public ResultVo addUser(User user) throws Exception {
        // 创建锁对象
        String lockKey = RedisConstant.USER_ADD_LOCK_PREFIX + user.getUsername();
        RLock lock = redissonClient.getLock(lockKey);

        // 尝试获取锁，使用看门狗模式（自动续期）
        boolean isLocked = lock.tryLock(0, -1, TimeUnit.SECONDS);
        if (!isLocked) {
            log.warn("获取分布式锁失败，用户名：{}", user.getUsername());
            return ResultVo.fail("系统繁忙，请稍后再试");
        }
        try {
            // 在锁保护下执行事务操作
            return transactionTemplate.execute(status -> {
                try {
                    // 校验用户名是否已存在
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("username", user.getUsername());
                    if (count(queryWrapper) > 0) {
                        status.setRollbackOnly();
                        throw new BusinessException("用户名已存在");
                    }

                    // 校验手机号是否已存在
                    queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("phone_number", user.getPhoneNumber());
                    if (count(queryWrapper) > 0) {
                        status.setRollbackOnly();
                        throw new BusinessException("手机号已存在");
                    }

                    // 校验邮箱是否已存在
                    queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("email", user.getEmail());
                    if (count(queryWrapper) > 0) {
                        status.setRollbackOnly();
                        throw new BusinessException("邮箱已存在");
                    }
                    // 插入用户
                    user.setIsDeleted(0);
                    user.setStatus(StatusConstant.ENABLE);
                    user.setPassword(passwordEncoder.encode(user.getPassword()));

                    int row = userMapper.insert(user);
                    if (row <= 0) {
                        status.setRollbackOnly();// 设置回滚
                        throw new BusinessException("添加失败");
                    }
                    return ResultVo.ok("添加成功");
                } catch (BusinessException e) {
                    throw e;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    throw new BusinessException("添加失败：" + e.getMessage());
                }
            });
        } finally {
            // 释放锁（只有当前线程持有锁时才释放）
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo updateUser(User user) throws Exception {
        UpdateWrapper<User> uw = new UpdateWrapper<>();
        uw.eq("id",user.getId());
        // 对密码进行加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            uw.set("password", encodedPassword);
        }
        user.setPassword(null);
        int row = userMapper.update(user, uw);
        if (row>0){
            return ResultVo.ok("修改成功");
        }
        return ResultVo.fail("修改失败");
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo deleteUser(Long id) throws Exception {
        UpdateWrapper<User> uw = new UpdateWrapper<>();
        uw.eq("id",id);
        uw.set("is_deleted",1);
        int row = userMapper.update(null, uw);
        if (row>0){
            return ResultVo.ok("删除成功");
        }
        return ResultVo.fail("删除失败");
    }

    @Override
    public ResultVo changePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 获取用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ResultVo.fail("用户不存在");
        }

        // 2. 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResultVo.fail("原密码错误");
        }

        // 3. 更新密码
        UpdateWrapper<User> uw = new UpdateWrapper<>();
        uw.eq("id", userId);
        uw.set("password", passwordEncoder.encode(newPassword));
        userMapper.update(null, uw);

        // 4. 清除该用户的所有登录token
        String tokenKey = RedisConstant.USER_TOKEN_PREFIX + userId;
        redisTemplate.delete(tokenKey);

        return ResultVo.ok("密码修改成功，请重新登录");
    }

    @Override
    public ResultVo loginWithCaptcha(LoginWithCodeDTO loginDTO) {

        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        String captcha = loginDTO.getCaptcha();
        String uuid = loginDTO.getUuid();

        // 1. 校验图片验证码
        String codeKey = RedisConstant.IMAGE_CODE_PREFIX + uuid;
        String savedCode = (String) redisTemplate.opsForValue().get(codeKey);

        if (savedCode == null) {
            return ResultVo.fail("验证码已过期，请刷新");
        }

        // 验证码比对（忽略大小写）
        if (!savedCode.equalsIgnoreCase(captcha)) {
            // 验证失败也删除 Key，防止暴力破解
            redisTemplate.delete(codeKey);
            return ResultVo.fail("验证码错误");
        }

        // 验证成功后立即删除验证码（防止重复使用）
        redisTemplate.delete(codeKey);

        // 2. 检查登录错误次数限制（防止暴力破解）
        String errorKey = RedisConstant.LOGIN_ERROR_PREFIX + username;
        Integer errorCount = (Integer) redisTemplate.opsForValue().get(errorKey);
        if (errorCount != null && errorCount >= RedisConstant.LOGIN_ERROR_LIMIT) {
            log.warn("用户 {} 登录错误次数过多，已被临时锁定", username);
            // 设置锁定时间
            redisTemplate.expire(errorKey, RedisConstant.LOGIN_ERROR_LOCK_MINUTES, TimeUnit.MINUTES);
            return ResultVo.fail("登录错误次数过多，请1小时后再试");
        }

        // 3. 原有的登录逻辑
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("username", username);
        User user = getOne(qw);

        if (user == null) {
            // 记录错误次数
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            // 记录错误次数
            recordLoginError(username);
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (user.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }
        if (user.getIsDeleted() == 1) {
            throw new BusinessException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 4. 登录成功，清除错误记录
        redisTemplate.delete(errorKey);

        // 5. 获取菜单
        if (user.getRoleId() != null) {
            List<Menu> menus = getUserMenus(user);
            user.setMenuList(menus);
        } else {
            return ResultVo.fail("无角色，请联系管理员");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, user.getId());
        String token = JwtUtil.createToken(
                jwtProperties.getUserSecret(),
                jwtProperties.getUserExpire(),
                claims);

        // 将 token 和用户信息存入 Redis
        String tokenKey = RedisConstant.USER_TOKEN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(tokenKey, token, Duration.ofSeconds(RedisConstant.TOKEN_EXPIRE_SECONDS));

        // 发送登录通知
        // 发送WebSocket通知
        Map<String, Object> message = new HashMap<>();
        message.put("type", 1);//1登录成功,2修改密码成功
        message.put("userId", user.getId());
        message.put("message", "登录成功，欢迎使用本颐养中心系统");
        webSocketServer.sendToUser(user.getId().toString(),JSON.toJSONString(message));

        log.info("用户 {} 登录成功", username);
        return ResultVo.ok(user, token);
    }

    /**
     * 获取用户菜单
     */
    private List<Menu> getUserMenus(User user) {
        //根据用户角色获得当前用户的菜单
        //1.获取得到角色对应的menu_id
        QueryWrapper listRoleQw = new QueryWrapper<>();
        listRoleQw.eq("role_id",user.getRoleId());
        listRoleQw.select("menu");
        List<Integer> menuIds = roleMenuMapper.selectObjs(listRoleQw);//管理员 1、2、3、4、5、6
        //2.根据menu_id获取得到一级菜单列表
        List<Menu> menus = menuMapper.selectBatchIds(menuIds);//1床位管理 2客户管理
        //3.查询子菜单
        for (Menu menu : menus) {
            QueryWrapper listMenuQw = new QueryWrapper<>();
            listMenuQw.eq("parent_id", menu.getId());//1
            menu.setChildren(menuMapper.selectList(listMenuQw));
        }
        user.setMenuList(menus);
        return menus;
    }

    @Override
    public ResultVo forgotPassword(ForgotPasswordDTO forgotDTO) {
        String email = forgotDTO.getEmail();

        // 1. 校验图片验证码
        String codeKey = RedisConstant.IMAGE_CODE_PREFIX + forgotDTO.getUuid();
        Object savedCodeObj = redisTemplate.opsForValue().get(codeKey);
        String savedCode = savedCodeObj != null ? savedCodeObj.toString() : null;


        if (savedCode == null) {
            return ResultVo.fail("验证码已过期，请刷新");
        }

        if (!savedCode.equalsIgnoreCase(forgotDTO.getCaptcha())) {
            return ResultVo.fail("验证码错误");
        }

        // 验证成功后删除验证码
        redisTemplate.delete(codeKey);

        // 2. 根据邮箱查询用户
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("email", email);
        qw.eq("is_deleted", 0);
        List<User> userList = list(qw);

        if (userList == null || userList.isEmpty()) {
            return ResultVo.fail("该邮箱未注册");
        }

        if (userList.size() > 1) {
            log.error("发现重复的用户邮箱：{}, 记录数：{}", email, userList.size());
            return ResultVo.fail("系统数据异常，存在重复邮箱，请联系管理员");
        }

        User user = userList.get(0);
        if (user == null) {
            return ResultVo.fail("该邮箱未注册");
        }

        // 3. 检查频率限制（防止恶意请求）
        String limitKey = RedisConstant.PASSWORD_RESET_PREFIX + "limit:" + email;
        Object lastSendTimeObj = redisTemplate.opsForValue().get(limitKey);

        if (lastSendTimeObj != null) {
            String lastSendTime = lastSendTimeObj.toString();
            long timeDiff = System.currentTimeMillis() - Long.parseLong(lastSendTime);
            if (timeDiff < 60000) {
                return ResultVo.fail("请求过于频繁，请稍后再试");
            }
        }

        // 4. 生成重置令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        String tokenKey = RedisConstant.PASSWORD_RESET_PREFIX + token;

        // 存储令牌，关联用户 ID 和邮箱
        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("userId", user.getId().toString());
        tokenData.put("email", user.getEmail());

        redisTemplate.opsForValue().set(tokenKey,
                JSON.toJSONString(tokenData),
                Duration.ofSeconds(RedisConstant.PASSWORD_RESET_EXPIRE));

        // 5. 记录发送时间
        redisTemplate.opsForValue().set(limitKey,
                String.valueOf(System.currentTimeMillis()),
                Duration.ofSeconds(RedisConstant.PASSWORD_RESET_EXPIRE));

        // 6. 构建重置链接（前端页面）
        String resetUrl = "http://localhost:8080/reset-password?token=" + token;

        // 7. 发送邮件
        try {
            mailService.sendPasswordResetEmail(user.getEmail(), resetUrl, user.getUsername());
            log.info("密码重置邮件发送成功，用户：{}", user.getUsername());
            return ResultVo.ok("重置链接已发送到您的邮箱，请查收");
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            return ResultVo.fail("邮件发送失败，请稍后重试");
        }
    }

    @Override
    public ResultVo resetPassword(ResetPasswordDTO resetDTO) {
        // 1. 校验新密码和确认密码是否一致
        if (!resetDTO.getNewPassword().equals(resetDTO.getConfirmPassword())) {
            return ResultVo.fail("两次输入的密码不一致");
        }

        // 2. 校验令牌
        String tokenKey = RedisConstant.PASSWORD_RESET_PREFIX + resetDTO.getToken();
        String tokenDataStr = redisTemplate.opsForValue().get(tokenKey).toString();

        if (tokenDataStr == null) {
            return ResultVo.fail("重置链接已失效，请重新申请");
        }

        // 3. 解析令牌数据
        Map<String, String> tokenData = JSON.parseObject(tokenDataStr, new TypeReference<Map<String, String>>(){});
        Long userId = Long.parseLong(tokenData.get("userId"));

        // 4. 更新密码
        User user = getById(userId);
        if (user == null) {
            return ResultVo.fail("用户不存在");
        }

        String encodedPassword = passwordEncoder.encode(resetDTO.getNewPassword());

        UpdateWrapper<User> uw = new UpdateWrapper<>();
        uw.eq("id", userId);
        uw.set("password", encodedPassword);
        uw.set("update_time", LocalDateTime.now());

        boolean updated = update(uw);

        if (updated) {
            // 5. 删除已使用的令牌
            redisTemplate.delete(tokenKey);

            // 6. 删除该用户的所有登录 token（强制下线）
            String tokenPattern = RedisConstant.USER_TOKEN_PREFIX + userId + "*";
            Set<String> tokens = redisTemplate.keys(tokenPattern);
            if (tokens != null && !tokens.isEmpty()) {
                redisTemplate.delete(tokens);
            }

            log.info("密码重置成功，用户 ID：{}", userId);
            return ResultVo.ok("密码重置成功，请使用新密码登录");
        }

        return ResultVo.fail("密码重置失败");
    }

    @Override
    public ResultVo verifyResetToken(String token) {
        // 构建令牌 key
        String tokenKey = RedisConstant.PASSWORD_RESET_PREFIX + token;
        // 获取令牌数据
        Object tokenDataObj = redisTemplate.opsForValue().get(tokenKey);
        String tokenData = tokenDataObj != null ? tokenDataObj.toString() : null;

        if (tokenData == null) {
            return ResultVo.fail("令牌已失效");
        }

        return ResultVo.ok("令牌有效");
    }
}
