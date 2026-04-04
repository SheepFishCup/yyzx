package com.cqupt.service.consumer;

import com.cqupt.config.RabbitMQConfig;
import com.cqupt.dto.MailMessage;
import com.cqupt.mapper.FailedMailRecordMapper;
import com.cqupt.pojo.FailedMailRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Component
public class MailDlxConsumer {

    @Autowired
    private FailedMailRecordMapper failedMailRecordMapper;

    @RabbitListener(queues = RabbitMQConfig.MAIL_DLX_QUEUE)
    public void handleDlxMail(MailMessage mailMessage) {
        log.error("🚨 死信队列 - 邮件发送失败，已记录到数据库：to={}, subject={}",
                mailMessage.getTo(),
                mailMessage.getSubject());

        try {
            FailedMailRecord record = new FailedMailRecord();
            record.setRecipient(mailMessage.getTo());
            record.setSubject(mailMessage.getSubject());
            record.setContent(mailMessage.getContent());
            record.setFailReason("邮件发送失败，进入死信队列");
            record.setStatus(0);
            record.setRetryCount(0);
            record.setNextRetryTime(new Date(System.currentTimeMillis() + 5 * 60 * 1000));

            failedMailRecordMapper.insert(record);
            log.info("✅ 失败邮件已保存到数据库，recordId={}", record.getId());
        } catch (Exception e) {
            log.error("❌ 保存失败记录到数据库失败：{}", e.getMessage(), e);
        }
    }
}
