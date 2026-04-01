package com.cqupt.config;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/21 22:54
 * @description
 */

import com.cqupt.task.BackdownAutoApproveJob;
import com.cqupt.task.GenerateWeeklyReportJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfig {
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
    public Scheduler scheduler(JobDetail processPendingBackdownJobDetail,
                               Trigger processPendingBackdownTrigger,
                               JobDetail generateWeeklyReportJobDetail,
                               Trigger generateWeeklyReportTrigger) throws SchedulerException {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        // 注册 JobDetails
        schedulerFactoryBean.setJobDetails(
                processPendingBackdownJobDetail,
                generateWeeklyReportJobDetail
        );
        // 注册 Triggers
        schedulerFactoryBean.setTriggers(
                processPendingBackdownTrigger,
                generateWeeklyReportTrigger
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
