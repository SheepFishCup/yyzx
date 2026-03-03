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
import com.cqupt.constant.JwtClaimsConstant;
import com.cqupt.constant.MessageConstant;
import com.cqupt.constant.PasswordConstant;
import com.cqupt.constant.StatusConstant;
import com.cqupt.context.BaseContext;
import com.cqupt.dto.UserDTO;
import com.cqupt.exception.AccountLockedException;
import com.cqupt.exception.AccountNotFoundException;
import com.cqupt.exception.PasswordErrorException;
import com.cqupt.mapper.MenuMapper;
import com.cqupt.mapper.RoleMenuMapper;
import com.cqupt.mapper.UserMapper;
import com.cqupt.pojo.Menu;
import com.cqupt.pojo.User;
import com.cqupt.properties.JwtProperties;
import com.cqupt.service.UserService;
import com.cqupt.utils.JwtUtil;
import com.cqupt.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public ResultVo<User> login(String username, String password) throws Exception {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("username", username);
        User user = getOne(qw);//获取用户
        if (user==null){
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user.getPassword())){
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        if (user.getIsDeleted()== StatusConstant.DISABLE){
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
                return ResultVo.ok(user, token);
            }
            return ResultVo.fail("无权限，请联系管理员");
        }
        return ResultVo.fail("用户名或密码错误");

    }

    @Override
    public ResultVo<Page<User>> findUserPage(UserDTO userDTO) throws Exception {
        Page<User> MyPage = new Page<>(userDTO.getPageSize(),3);
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (userDTO.getNickName()!=null && userDTO.getNickName()!=""){
            qw.like("nickname",userDTO.getNickName());
        }
        qw.eq("role_id",userDTO.getRoleId());
        qw.eq("is_deleted",0);
        page(MyPage,qw);
        return ResultVo.ok(MyPage);
    }

    @Override
    public ResultVo<Page<User>> findAllUserPage(UserDTO userDTO) throws Exception {
        Page<User> myPage = new Page<>(userDTO.getPageSize(),6);
        QueryWrapper<User> qw = new QueryWrapper<>();
        page(myPage,qw);
        return ResultVo.ok(myPage);
    }

    @Override
    public ResultVo addUser(User user) throws Exception {
        String lockKey = "lock:user:add:" + user.getUsername();//锁的key
        try {
            // 尝试获取分布式锁
            //锁的过期时间设置为10秒
            if (!redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(10))) {
                return ResultVo.fail("系统繁忙，请稍后再试");
            }
            // 校验用户名是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", user.getUsername());
            User existingUser = getOne(queryWrapper);

            if (existingUser != null) {
                throw new RuntimeException("用户名已存在");
            }
//            BeanUtils.copyProperties(UserDTO, user);
            // 插入用户
            user.setIsDeleted(StatusConstant.ENABLE);
            user.setUpdateBy(BaseContext.getCurrentId());
            user.setCreateBy(BaseContext.getCurrentId());
            // 设置默认密码
//            user.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
            // 对密码进行MD5加密
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));

            int row = userMapper.insert(user);
            if (row <= 0) {
                return ResultVo.fail("添加失败");
            }
            return ResultVo.ok("添加成功");
        } finally {
            // 释放分布式锁
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public ResultVo updateUser(User user) throws Exception {
        UpdateWrapper<User> uw = new UpdateWrapper<>();
        uw.eq("id",user.getId());
        int row = userMapper.update(user, uw);
        if (row>0){
            return ResultVo.ok("修改成功");
        }
        return ResultVo.fail("修改失败");
    }

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
}
