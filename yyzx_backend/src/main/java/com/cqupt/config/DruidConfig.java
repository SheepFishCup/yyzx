package com.cqupt.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.alibaba.druid.wall.WallFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class DruidConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public DataSource druidDataSource() {
        return new DruidDataSource();
//        DruidDataSource dataSource = new DruidDataSource();
//        dataSource.setUrl("jdbc:mysql://localhost:3306/yyzx?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&useSSL=false");
//        dataSource.setUsername("root");
//        dataSource.setPassword("123456");
//        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
//
//        dataSource.setInitialSize(5);//初始化连接数
//        dataSource.setMinIdle(2);//最小连接数
//        dataSource.setMaxActive(20);//最大连接数
//        dataSource.setMaxWait(3000);//等待连接超时时间
//        dataSource.setTimeBetweenEvictionRunsMillis(60000);//配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
//        dataSource.setPoolPreparedStatements(true);//配置是否缓存preparedStatement，也就是PSCache
//        dataSource.setMinEvictableIdleTimeMillis(300000);//配置连接在池中的最小生存时间
//        dataSource.setValidationQuery("SELECT 1");//配置检测连接是否有效的sql
//        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);//配置了最大连接数，超过配置的maxActive之后，extraPreparedStatements会自动关闭
//        dataSource.setTestWhileIdle(true);//配置检测连接是否有效
//        dataSource.setTestOnBorrow(false);//配置连接池中连接是否被使用前校验
//        dataSource.setTestOnReturn(false);//配置连接池中连接是否被使用后校验
//
//        // 关键：手动添加 StatFilter 统计过滤器
//        StatFilter statFilter = new StatFilter();
//        statFilter.setMergeSql(true);
//        statFilter.setSlowSqlMillis(1000);//慢查询阈值为1秒
//        statFilter.setLogSlowSql(true);
//
//        // 添加 WallFilter 防止 SQL 注入
//        WallFilter wallFilter = new WallFilter();
//        wallFilter.setDbType("mysql");
//
//        // 将 StatFilter 添加到数据源
//        dataSource.getProxyFilters().add(statFilter);
//        dataSource.getProxyFilters().add(wallFilter);
//
//        // 添加防止内存泄漏的关键配置
//        dataSource.setRemoveAbandoned(true); // 删除泄露的连接
//        dataSource.setRemoveAbandonedTimeout(300); // 连接泄露超时时间(秒)
//        dataSource.setLogAbandoned(true); // 记录泄露日志
//
//        return dataSource;
    }
    
    /**
     * 配置Druid监控界面
     */
    @Bean
    public ServletRegistrationBean<StatViewServlet> statViewServlet() {
        ServletRegistrationBean<StatViewServlet> bean = new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
        Map<String, String> initParams = new HashMap<>();
        
        // 配置监控页面登录用户名和密码
        initParams.put("loginUsername", "admin");
        initParams.put("loginPassword", "admin");
        // 是否可以重置数据
        initParams.put("resetEnable", "false");
        
        bean.setInitParameters(initParams);//设置初始化参数
        return bean;
    }
    
    /**
     * 配置Web监控过滤器
     */
    @Bean
    public FilterRegistrationBean<WebStatFilter> webStatFilter() {
        FilterRegistrationBean<WebStatFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new WebStatFilter());

        Map<String, String> initParams = new HashMap<>();
        // 忽略资源
        initParams.put("exclusions", "*.js,*.css,*.jpg,*.png,*.ico,/druid/*");
        bean.setInitParameters(initParams);

        bean.addUrlPatterns("/*");
        return bean;
    }
}