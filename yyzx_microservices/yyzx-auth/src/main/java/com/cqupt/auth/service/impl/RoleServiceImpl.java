package com.cqupt.auth.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/07/01 08:44
 * @description
 */

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.auth.mapper.RoleMapper;
import com.cqupt.pojo.Role;
import com.cqupt.auth.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

}
