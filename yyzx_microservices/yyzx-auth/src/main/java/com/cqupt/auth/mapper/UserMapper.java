package com.cqupt.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper extends BaseMapper<User> {
    Page<User> selectUserPage(@Param("page") Page<User> page,
                              @Param("nickName") String nickName,
                              @Param("roleId") Integer roleId);
}
