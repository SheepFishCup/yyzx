package com.cqupt.task;

import com.cqupt.service.MailService;
import com.cqupt.utils.RedisDelayQueueUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DelayQueueProcessor extends QuartzJobBean {

    @Autowired
    private RedisDelayQueueUtils delayQueueUtils;

    @Autowired
    private MailService mailService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.info("⏰ 开始处理延迟队列任务...");

        // 处理邮件发送延迟队列
        processMailDelayQueue();

        // 可以添加更多延迟队列处理逻辑
    }

    /**
     * 处理邮件延迟队列
     */
    private void processMailDelayQueue() {
        String queueName = "yyzx:delay:mail:queue";
        
        try {
            // 获取所有到期的任务
            List<String> tasks = delayQueueUtils.getDueTasks(queueName, 50);
            
            if (tasks == null || tasks.isEmpty()) {
                log.debug("邮件延迟队列为空");
                return;
            }

            for (String task : tasks) {
                try {
                    // TODO: 解析任务数据并执行
                    // Map<String, Object> taskData = objectMapper.readValue(task, Map.class);
                    // mailService.sendEmail(...);
                    
                    // 移除已处理的任务
                    delayQueueUtils.removeTask(queueName, task);
                    log.info("✅ 延迟邮件任务处理完成：{}", task);
                    
                } catch (Exception e) {
                    log.error("❌ 处理延迟邮件任务失败：{}", task, e);
                }
            }
            
            log.info("📊 邮件延迟队列处理完成，共处理 {} 条", tasks.size());
            
        } catch (Exception e) {
            log.error("处理邮件延迟队列异常", e);
        }
    }
}
