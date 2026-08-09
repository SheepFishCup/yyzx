package com.cqupt.auth.service.impl;
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
import com.cqupt.dto.*;
import com.cqupt.exception.AccountLockedException;
import com.cqupt.exception.AccountNotFoundException;
import com.cqupt.exception.BusinessException;
import com.cqupt.exception.PasswordErrorException;
import com.cqupt.auth.mapper.MenuMapper;
import com.cqupt.auth.mapper.RoleMenuMapper;
import com.cqupt.auth.mapper.UserMapper;
import com.cqupt.pojo.Menu;
import com.cqupt.pojo.User;
import com.cqupt.properties.JwtProperties;
import com.cqupt.auth.service.UserService;
import com.cqupt.rabbit.RabbitMQProducerService;
import com.cqupt.utils.HybridBlacklistUtils;
import com.cqupt.utils.JwtUtil;
import com.cqupt.utils.ResultVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    private HybridBlacklistUtils blacklistUtils;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private RabbitMQProducerService rabbitMQProducerService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final ConcurrentHashMap<String, Object> LOCAL_CACHE = new ConcurrentHashMap<>();
    private static final boolean REDIS_FALLBACK_ENABLED = true;
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
        if (REDIS_FALLBACK_ENABLED) {
            LOCAL_CACHE.put(key, new CacheEntry(value, ttlMillis));
        }
    }

    private Object getFromLocalCache(String key) {
        if (!REDIS_FALLBACK_ENABLED) {
            return null;
        }
        CacheEntry entry = (CacheEntry) LOCAL_CACHE.get(key);
        if (entry == null || entry.isExpired()) {
            LOCAL_CACHE.remove(key);
            return null;
        }
        return entry.value;
    }

    private void removeFromLocalCache(String key) {
        if (REDIS_FALLBACK_ENABLED) {
            LOCAL_CACHE.remove(key);
        }
    }
    // 记录登录错误（带降级）
    private void recordLoginError(String username) {
        String key = RedisConstant.LOGIN_ERROR_PREFIX + username;
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count == 1) {
                redisTemplate.expire(key, Duration.ofHours(1));
            }
            if (count >= 5) {
                lockAccount(username);
            }
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败，使用本地缓存记录错误次数", e);
            Integer count = (Integer) getFromLocalCache(key);
            if (count == null) {
                count = 0;
            }
            count++;
            putToLocalCache(key, count, Duration.ofHours(1).toMillis());
            if (count >= 5) {
                lockAccount(username);
            }
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
        String lockKey = RedisConstant.USER_ADD_LOCK_PREFIX + user.getUsername();
        RLock lock = null;

        try {
            lock = redissonClient.getLock(lockKey);
            boolean isLocked = lock.tryLock(0, -1, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("获取分布式锁失败，用户名：{}", user.getUsername());
                return ResultVo.fail("系统繁忙，请稍后再试");
            }

            return executeAddUser(user);

        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败，使用本地锁降级", e);
            synchronized (this) {
                return executeAddUser(user);
            }
        } finally {
            if (lock != null && lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    log.error("释放锁失败", e);
                }
            }
        }
    }

    private ResultVo executeAddUser(User user) {
        // 检查用户名、手机号、邮箱是否已存在
        return transactionTemplate.execute(status -> {
            try {
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("username", user.getUsername());
                if (count(queryWrapper) > 0) {
                    status.setRollbackOnly();
                    throw new BusinessException("用户名已存在");
                }

                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("phone_number", user.getPhoneNumber());
                if (count(queryWrapper) > 0) {
                    status.setRollbackOnly();
                    throw new BusinessException("手机号已存在");
                }

                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("email", user.getEmail());
                if (count(queryWrapper) > 0) {
                    status.setRollbackOnly();
                    throw new BusinessException("邮箱已存在");
                }

                user.setIsDeleted(0);
                user.setStatus(StatusConstant.ENABLE);
                user.setPassword(passwordEncoder.encode(user.getPassword()));

                int row = userMapper.insert(user);
                if (row <= 0) {
                    status.setRollbackOnly();
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

        // 黑名单检查（带降级）
        try {
            if (blacklistUtils.isInBlacklist(username)) {
                log.warn("用户 {} 在黑名单中，拒绝登录", username);
                return ResultVo.fail("该账号已被限制登录");
            }
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败，跳过黑名单检查", e);
        }

        String codeKey = RedisConstant.IMAGE_CODE_PREFIX + uuid;
        String savedCode = null;
        boolean fromRedis = true;

        try {
            savedCode = (String) redisTemplate.opsForValue().get(codeKey);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败，使用本地缓存验证验证码", e);
            fromRedis = false;
            savedCode = (String) getFromLocalCache(codeKey);
        }
        if (savedCode == null) {
            return ResultVo.fail("验证码已过期，请刷新");
        }

        if (!savedCode.equalsIgnoreCase(captcha)) {
            if (fromRedis) {
                try {
                    redisTemplate.delete(codeKey);
                } catch (Exception e) {
                    log.error("删除验证码失败", e);
                }
            } else {
                removeFromLocalCache(codeKey);
            }
            return ResultVo.fail("验证码错误");
        }

        if (fromRedis) {
            try {
                redisTemplate.delete(codeKey);
            } catch (Exception e) {
                log.error("删除验证码失败", e);
            }
        } else {
            removeFromLocalCache(codeKey);
        }

        // 错误次数检查（带降级）
        String errorKey = RedisConstant.LOGIN_ERROR_PREFIX + username;
        Integer errorCount = null;
        try {
            errorCount = (Integer) redisTemplate.opsForValue().get(errorKey);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败，跳过错误次数检查", e);
            errorCount = (Integer) LOCAL_CACHE.get(errorKey);
        }
        if (errorCount != null && errorCount >= RedisConstant.LOGIN_ERROR_LIMIT) {
            try {
                if (fromRedis) {
                    redisTemplate.expire(errorKey, RedisConstant.LOGIN_ERROR_LOCK_MINUTES, TimeUnit.MINUTES);
                    blacklistUtils.addToBlacklist(username);
                }
            } catch (Exception e) {
                log.error("锁定账户失败", e);
            }
            log.warn("用户 {} 登录错误次数过多，已被锁定", username);
            return ResultVo.fail("登录错误次数过多，请 1 小时后再试");
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
            try {
                recordLoginError(username);
            } catch (Exception e) {
                log.error("记录登录错误失败", e);
            }
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (user.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }
        if (user.getIsDeleted() == 1) {
            throw new BusinessException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 4. 登录成功，清除错误记录
        try {
            redisTemplate.delete(errorKey);
        } catch (Exception e) {
            LOCAL_CACHE.remove(errorKey);
        }

        // 5. 获取菜单
        if (user.getRoleId() != null) {
            List<Menu> menus = getUserMenus(user);
            user.setMenuList(menus);
        } else {
            return ResultVo.fail("无角色，请联系管理员");
        }

        // JWT claims: 包含 userId、username、roleId 供 Gateway 无状态认证
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, user.getId());
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("roleId", user.getRoleId());
        String token = JwtUtil.createToken(
                jwtProperties.getUserSecret(),
                jwtProperties.getUserExpire(),
                claims);

        // 将 token 存入 Redis（可选：Redis 不可用时仍可正常认证）
        try {
            String tokenKey = RedisConstant.USER_TOKEN_PREFIX + user.getId();
            redisTemplate.opsForValue().set(tokenKey, token, Duration.ofSeconds(RedisConstant.TOKEN_EXPIRE_SECONDS));
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis 不可用，使用无状态 JWT 模式", e);
        }
        // 发送登录通知（通过 RabbitMQ 异步推送 WebSocket 通知）
        Map<String, Object> message = new HashMap<>();
        message.put("type", 1);//1登录成功,2修改密码成功
        message.put("userId", user.getId());
        message.put("message", "登录成功，欢迎使用本颐养中心系统");
        NotifyMessage notifyMessage = new NotifyMessage();
        notifyMessage.setUserId(user.getId());
        notifyMessage.setType("SYSTEM");
        notifyMessage.setTitle("登录通知");
        notifyMessage.setContent("登录成功，欢迎使用本颐养中心系统");
        rabbitMQProducerService.sendNotify(notifyMessage);

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

        String codeKey = RedisConstant.IMAGE_CODE_PREFIX + forgotDTO.getUuid();
        String savedCode = null;
        boolean fromRedis = true;

        try {
            Object savedCodeObj = redisTemplate.opsForValue().get(codeKey);
            savedCode = savedCodeObj != null ? savedCodeObj.toString() : null;
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败，使用本地缓存验证验证码", e);
            fromRedis = false;
            Object cachedCode = getFromLocalCache(codeKey);
            savedCode = cachedCode != null ? cachedCode.toString() : null;
        }

        if (savedCode == null) {
            return ResultVo.fail("验证码已过期，请刷新");
        }

        if (!savedCode.equalsIgnoreCase(forgotDTO.getCaptcha())) {
            return ResultVo.fail("验证码错误");
        }

        if (fromRedis) {
            try {
                redisTemplate.delete(codeKey);
            } catch (Exception e) {
                log.error("删除验证码失败", e);
            }
        } else {
            removeFromLocalCache(codeKey);
        }

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

        String limitKey = RedisConstant.PASSWORD_RESET_PREFIX + "limit:" + email;
        try {
            Object lastSendTimeObj = redisTemplate.opsForValue().get(limitKey);
            if (lastSendTimeObj != null) {
                String lastSendTime = lastSendTimeObj.toString();
                long timeDiff = System.currentTimeMillis() - Long.parseLong(lastSendTime);
                if (timeDiff < 60000) {
                    return ResultVo.fail("请求过于频繁，请稍后再试");
                }
            }
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败，跳过频率限制检查", e);
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        String tokenKey = RedisConstant.PASSWORD_RESET_PREFIX + token;

        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("userId", user.getId().toString());
        tokenData.put("email", user.getEmail());

        String tokenDataJson = JSON.toJSONString(tokenData);
        try {
            redisTemplate.opsForValue().set(tokenKey,
                    tokenDataJson,
                    Duration.ofSeconds(RedisConstant.PASSWORD_RESET_EXPIRE));
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败，使用本地缓存存储令牌", e);
            putToLocalCache(tokenKey, tokenDataJson, RedisConstant.PASSWORD_RESET_EXPIRE * 1000L);
        }

        try {
            redisTemplate.opsForValue().set(limitKey,
                    String.valueOf(System.currentTimeMillis()),
                    Duration.ofSeconds(RedisConstant.PASSWORD_RESET_EXPIRE));
        } catch (RedisConnectionFailureException e) {
            log.error("记录发送时间失败", e);
            putToLocalCache(limitKey, String.valueOf(System.currentTimeMillis()),
                    RedisConstant.PASSWORD_RESET_EXPIRE * 1000L);
        }

        String resetUrl = "http://localhost:8080/reset-password?token=" + token;

        sendResetEmailAsync(user.getEmail(), user.getUsername(), resetUrl);

        return ResultVo.ok("重置链接已发送到您的邮箱，请查收");
    }
    /**
     * 异步发送重置密码邮件（通过 RabbitMQ）
     */
    private void sendResetEmailAsync(String email, String username, String resetUrl) {
        try {
            // ✅ content 格式："username|resetUrl"
            String content = username + "|" + resetUrl;

            // 构建邮件消息
            MailMessage mailMessage = MailMessage.builder()
                    .to(email)
                    .subject("【养老护理系统】密码重置")
                    .content(content)
                    .build();

            // 发送到邮件队列
            rabbitMQProducerService.sendMail(mailMessage);

            log.info("已添加邮件发送任务到队列：email={}, username={}", email, username);

        } catch (Exception e) {
            log.error("添加邮件任务失败：email={}", email, e);
            // 注意：这里不抛出异常，不影响主流程
        }
    }
    @Override
    public ResultVo resetPassword(ResetPasswordDTO resetDTO) {
        // 1. 校验新密码和确认密码是否一致
        if (!resetDTO.getNewPassword().equals(resetDTO.getConfirmPassword())) {
            return ResultVo.fail("两次输入的密码不一致");
        }

        String tokenKey = RedisConstant.PASSWORD_RESET_PREFIX + resetDTO.getToken();
        String tokenDataStr = null;
        boolean fromRedis = true;

        try {
            Object tokenDataObj = redisTemplate.opsForValue().get(tokenKey);
            tokenDataStr = tokenDataObj != null ? tokenDataObj.toString() : null;
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败，使用本地缓存验证令牌", e);
            fromRedis = false;
            Object cachedToken = getFromLocalCache(tokenKey);
            tokenDataStr = cachedToken != null ? cachedToken.toString() : null;
        }

        if (tokenDataStr == null) {
            return ResultVo.fail("重置链接已失效，请重新申请");
        }

        Map<String, String> tokenData = JSON.parseObject(tokenDataStr, new TypeReference<Map<String, String>>(){});
        Long userId = Long.parseLong(tokenData.get("userId"));

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
            if (fromRedis) {
                try {
                    redisTemplate.delete(tokenKey);
                } catch (Exception e) {
                    log.error("删除令牌失败", e);
                }
            } else {
                removeFromLocalCache(tokenKey);
            }

            try {
                String tokenPattern = RedisConstant.USER_TOKEN_PREFIX + userId + "*";
                Set<String> tokens = redisTemplate.keys(tokenPattern);
                if (tokens != null && !tokens.isEmpty()) {
                    redisTemplate.delete(tokens);
                }
            } catch (RedisConnectionFailureException e) {
                log.warn("Redis 不可用，无法清除用户 Token，用户需等待自然过期", e);
            }

            log.info("密码重置成功，用户 ID：{}", userId);
            return ResultVo.ok("密码重置成功，请使用新密码登录");
        }

        return ResultVo.fail("密码重置失败");
    }

    @Override
    public ResultVo verifyResetToken(String token) {
        String tokenKey = RedisConstant.PASSWORD_RESET_PREFIX + token;
        String tokenData = null;

        try {
            Object tokenDataObj = redisTemplate.opsForValue().get(tokenKey);
            tokenData = tokenDataObj != null ? tokenDataObj.toString() : null;
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 连接失败，使用本地缓存验证令牌", e);
            Object cachedToken = getFromLocalCache(tokenKey);
            tokenData = cachedToken != null ? cachedToken.toString() : null;
        }

        if (tokenData == null) {
            return ResultVo.fail("令牌已失效");
        }

        return ResultVo.ok("令牌有效");
    }
}
