package com.cqupt.handler;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/12/16 20:47
 * @description
 */

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.cqupt.context.BaseContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;
//填充器
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("createTime", new Date(), metaObject);
        this.setFieldValByName("updateTime", new Date(), metaObject);

        Long userId = getCurrentUserId();
        if (userId != null) {
            this.setFieldValByName("createBy", userId, metaObject);
            this.setFieldValByName("updateBy", userId, metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", new Date(), metaObject);
        Long userId = getCurrentUserId();
        if (userId != null) {
            this.setFieldValByName("updateBy", userId, metaObject);
        }
    }
    private Long getCurrentUserId() {
        try {
            return BaseContext.getCurrentId();
        } catch (Exception e) {
            return null;
        }
    }
}
