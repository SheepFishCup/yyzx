package com.cqupt.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/07/01 10:06
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.constant.*;
import com.cqupt.context.BaseContext;
import com.cqupt.dto.LoginWithCodeDTO;
import com.cqupt.dto.UserDTO;
import com.cqupt.exception.AccountLockedException;
import com.cqupt.exception.AccountNotFoundException;
import com.cqupt.exception.BusinessException;
import com.cqupt.exception.PasswordErrorException;
import com.cqupt.mapper.MenuMapper;
import com.cqupt.mapper.RoleMenuMapper;
import com.cqupt.mapper.UserMapper;
import com.cqupt.pojo.Menu;
import com.cqupt.pojo.RoleMenu;
import com.cqupt.pojo.User;
import com.cqupt.properties.JwtProperties;
import com.cqupt.service.UserService;
import com.cqupt.utils.JwtUtil;
import com.cqupt.utils.ResultVo;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public ResultVo<User> login(String username, String password) throws Exception {
        // 1. 先从Redis缓存查询用户（避免每次都查库）
        String userKey = RedisConstant.USER_INFO_PREFIX + username;
        User user = (User) redisTemplate.opsForValue().get(userKey);
        if (user == null) {
            // 缓存不存在，查询数据库
            QueryWrapper<User> qw = new QueryWrapper<>();
            qw.eq("username", username);
            user = getOne(qw);
            if (user != null) {
                // 存入缓存（设置过期时间）
                redisTemplate.opsForValue().set(userKey, user,
                        Duration.ofHours(RedisConstant.USER_CACHE_HOURS));
            }
        }
        if (user==null){
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

//        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!passwordEncoder.matches(password, user.getPassword())){
            recordLoginError(username);
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        if (user.getStatus() == StatusConstant.DISABLE){
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }
        if (user.getRoleId()==null){
            return ResultVo.fail("无角色，请联系管理员");
        }
        if (user!=null){
            if (user.getIsDeleted()==0) {
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
                //如果登录验证成功，则需要生成令牌token(token就是按照特定规则生成的字符串)
                Map<String, Object> claims = new HashMap<>();
                claims.put(JwtClaimsConstant.EMP_ID, user.getId());
                String token = JwtUtil.createToken(
                        jwtProperties.getUserSecret(),
                        jwtProperties.getUserExpire(),
                        claims);

                // 将 token 和用户信息存入 Redis
                String tokenKey = RedisConstant.USER_TOKEN_PREFIX + user.getId();
                redisTemplate.opsForValue().set(tokenKey, token, Duration.ofSeconds(RedisConstant.TOKEN_EXPIRE_SECONDS));

                return ResultVo.ok(user, token);
            }
            return ResultVo.fail("无权限，请联系管理员");
        }
        return ResultVo.fail("用户名或密码错误");

    }
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
        Page<User> MyPage = new Page<>(userDTO.getPageSize(),6);
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (userDTO.getNickName()!=null && !"".equals(userDTO.getNickName())){
            qw.like("nickname",userDTO.getNickName());
        }
        qw.eq("role_id",userDTO.getRoleId());
        qw.eq("is_deleted",0);
        page(MyPage,qw);
        return ResultVo.ok(MyPage);
    }

    @Override
    public ResultVo<Page<User>> findAllUserPage(UserDTO userDTO) throws Exception {
        // 检查当前用户是否有权限查看
        Long currentId = BaseContext.getCurrentId();
        User currentUser = userMapper.selectById(currentId);
        if (currentUser.getRoleId() != 1) { // 假设 1 是管理员
            throw new AccessDeniedException("无权访问");
        }
        Page<User> myPage = new Page<>(userDTO.getPageSize(),6);
        QueryWrapper<User> qw = new QueryWrapper<>();
        page(myPage,qw);
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
                    User existingUser = getOne(queryWrapper);
                    if (existingUser != null) {
                        status.setRollbackOnly();
                        throw new BusinessException("用户名已存在");
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
}
