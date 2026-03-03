package com.cqupt.config;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/02 14:03
 * @description
 */

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceShutdownConfig implements DisposableBean {

    @Autowired
    private DataSource dataSource;

    @Override
    public void destroy() throws Exception {
        if (dataSource instanceof DruidDataSource) {
            ((DruidDataSource) dataSource).close();
        }
    }
}
