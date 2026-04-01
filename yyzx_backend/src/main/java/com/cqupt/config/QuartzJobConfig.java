package com.cqupt.config;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/21 22:59
 * @description
 */


import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.annotation.PostConstruct;

@Configuration
public class QuartzJobConfig {

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        schedulerFactoryBean.setJobFactory(new SpringBeanJobFactory() {
            @Override
            protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
                Object jobInstance = super.createJobInstance(bundle);
                AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
                beanFactory.autowireBean(jobInstance);
                return jobInstance;
            }
        });
    }
}