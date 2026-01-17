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
import com.cqupt.dto.UserDTO;
import com.cqupt.mapper.MenuMapper;
import com.cqupt.mapper.RoleMapper;
import com.cqupt.mapper.RoleMenuMapper;
import com.cqupt.mapper.UserMapper;
import com.cqupt.pojo.Menu;
import com.cqupt.pojo.RoleMenu;
import com.cqupt.pojo.User;
import com.cqupt.service.UserService;
import com.cqupt.utils.ResultVo;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private MenuMapper menuMapper;
    @Autowired
    private RoleMenuMapper roleMenuMapper;

    @Override
    public ResultVo<User> login(String username, String password) throws Exception {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("username",username);
        qw.eq("password",password);
        User user = getOne(qw);
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
                HashMap<String, Object> map = new HashMap<>();
                //如果登录验证成功，则需要生成令牌token(token就是按照特定规则生成的字符串)
                JwtBuilder builder = Jwts.builder();
                String token = builder.setSubject(username)
                        .setIssuedAt(new Date())//设置token的生成时间
                        .setId(user.getId().toString())
                        .setClaims(map)
                        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))//24小时
                        .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256, "cqupt123456")//签名
                        .compact();

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
        user.setIsDeleted(0);
        int row =userMapper.insert(user);
        if (row<=0){
            return ResultVo.fail("添加失败");
        }
        return ResultVo.ok("添加成功");
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
    public ResultVo deleteUser(Integer id) throws Exception {
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
