package com.cqupt.task.job;

import com.cqupt.utils.RedisDelayQueueUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Redis 延迟队列处理器 — 每分钟执行，处理到期的延迟任务
 * <p>重写版：移除未使用的 MailService 依赖</p>
 */
@Slf4j
@Component
public class DelayQueueProcessor extends QuartzJobBean {

    @Autowired
    private RedisDelayQueueUtils delayQueueUtils;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.debug("处理 Redis 延迟队列任务...");

        String queueName = "yyzx:delay:mail:queue";

        try {
            List<String> tasks = delayQueueUtils.getDueTasks(queueName, 50);

            if (tasks == null || tasks.isEmpty()) {
                return;
            }

            for (String task : tasks) {
                try {
                    delayQueueUtils.removeTask(queueName, task);
                    log.info("延迟任务处理完成：{}", task);
                } catch (Exception e) {
                    log.error("处理延迟任务失败：{}", task, e);
                }
            }

            log.info("延迟队列处理完成，共 {} 条", tasks.size());
        } catch (Exception e) {
            log.error("处理延迟队列异常", e);
        }
    }
}
