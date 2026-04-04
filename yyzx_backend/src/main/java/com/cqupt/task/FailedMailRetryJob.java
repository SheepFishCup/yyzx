package com.cqupt.task;

import com.cqupt.pojo.FailedMailRecord;
import com.cqupt.service.FailedMailService;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FailedMailRetryJob extends QuartzJobBean {

    private static FailedMailService failedMailService;

    @Autowired
    public void setFailedMailService(FailedMailService failedMailService) {
        FailedMailRetryJob.failedMailService = failedMailService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.info("🕐 开始执行失败邮件重试任务...");

        try {
            var records = failedMailService.findDueRecords();
            int successCount = 0;
            int failCount = 0;

            for (FailedMailRecord record : records) {
                try {
                    failedMailService.retryMail(record);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("重试失败：recordId={}, to={}, error={}",
                            record.getId(), record.getRecipient(), e.getMessage());
                }
            }

            log.info("🏁 失败邮件重试任务完成，共处理{}条，成功{}条，失败{}条",
                    records.size(), successCount, failCount);

        } catch (Exception e) {
            log.error("执行失败邮件重试任务时发生异常", e);
        }
    }
}
