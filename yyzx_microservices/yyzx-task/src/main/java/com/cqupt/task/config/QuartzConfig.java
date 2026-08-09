package com.cqupt.task.config;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/21 22:54
 * @description
 */

import com.cqupt.task.job.BackdownAutoApproveJob;
import com.cqupt.task.job.DelayQueueProcessor;
import com.cqupt.task.job.FailedMailRetryJob;
import com.cqupt.task.job.GenerateWeeklyReportJob;
import org.quartz.*;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Configuration
public class QuartzConfig {
    @Autowired
    private ApplicationContext applicationContext;
    @Bean
    public JobDetail processPendingBackdownJobDetail() {
        return JobBuilder.newJob(BackdownAutoApproveJob.class)
                .withIdentity("processPendingBackdown", "businessJobs")
                .withDescription("处理待审批退住申请")
                .storeDurably()
                .build();
    }

    // 每 1 小时执行一次
    @Bean
    public Trigger processPendingBackdownTrigger() {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0 0 * * * ?");
        return TriggerBuilder.newTrigger()
                .forJob(processPendingBackdownJobDetail())
                .withIdentity("processPendingBackdownTrigger", "businessTriggers")
                .withSchedule(scheduleBuilder)
                .build();
    }

    @Bean
    public JobDetail generateWeeklyReportJobDetail() {
        return JobBuilder.newJob(GenerateWeeklyReportJob.class)
                .withIdentity("generateWeeklyReport", "businessJobs")
                .withDescription("生成周报统计")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger generateWeeklyReportTrigger() {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0 0 8 ? * MON");
        return TriggerBuilder.newTrigger()
                .forJob(generateWeeklyReportJobDetail())
                .withIdentity("generateWeeklyReportTrigger", "businessTriggers")
                .withSchedule(scheduleBuilder)
                .build();
    }

    @Bean
    public JobDetail failedMailRetryJobDetail() {
        return JobBuilder.newJob(FailedMailRetryJob.class)
                .withIdentity("failedMailRetry", "mailJobs")
                .withDescription("失败邮件自动重试")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger failedMailRetryTrigger() {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0 */5 * * * ?");
        return TriggerBuilder.newTrigger()
                .forJob(failedMailRetryJobDetail())
                .withIdentity("failedMailRetryTrigger", "mailTriggers")
                .withSchedule(scheduleBuilder)
                .build();
    }
    @Bean
    public JobDetail delayQueueProcessorJobDetail() {
        return JobBuilder.newJob(DelayQueueProcessor.class)
                .withIdentity("delayQueueProcessor", "delayJobs")
                .withDescription("处理 Redis 延迟队列")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger delayQueueProcessorTrigger() {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0 */1 * * * ?");
        return TriggerBuilder.newTrigger()
                .forJob(delayQueueProcessorJobDetail())
                .withIdentity("delayQueueProcessorTrigger", "delayTriggers")
                .withSchedule(scheduleBuilder)
                .build();
    }

    @Bean
    public Scheduler scheduler(JobDetail processPendingBackdownJobDetail,
                               Trigger processPendingBackdownTrigger,
                               JobDetail generateWeeklyReportJobDetail,
                               Trigger generateWeeklyReportTrigger,
                               JobDetail failedMailRetryJobDetail,
                               Trigger failedMailRetryTrigger,
                               JobDetail delayQueueProcessorJobDetail,
                               Trigger delayQueueProcessorTrigger) throws SchedulerException {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory() {
            @Override
            protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
                Object jobInstance = super.createJobInstance(bundle);
                AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
                beanFactory.autowireBean(jobInstance);
                return jobInstance;
            }
        };
        schedulerFactoryBean.setJobFactory(jobFactory);

        schedulerFactoryBean.setJobDetails(
                processPendingBackdownJobDetail,
                generateWeeklyReportJobDetail,
                failedMailRetryJobDetail,
                delayQueueProcessorJobDetail
        );
        schedulerFactoryBean.setTriggers(
                processPendingBackdownTrigger,
                generateWeeklyReportTrigger,
                failedMailRetryTrigger,
                delayQueueProcessorTrigger
        );
        schedulerFactoryBean.setAutoStartup(true);
        try {
            schedulerFactoryBean.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException("Quartz Scheduler 初始化失败", e);
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        scheduler.start();
        return scheduler;
    }
}
