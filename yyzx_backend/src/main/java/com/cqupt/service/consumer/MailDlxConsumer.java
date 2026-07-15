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
            // 1. 查找是否存在未完成的失败记录（通过收件人和主题匹配）
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FailedMailRecord> qw =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            qw.eq("recipient", mailMessage.getTo())
                    .eq("subject", mailMessage.getSubject())
                    .in("status", 0, 1) // 查找待重试或重试中的记录
                    .orderByDesc("create_time")
                    .last("LIMIT 1");

            FailedMailRecord lastRecord = failedMailRecordMapper.selectOne(qw);

            if (lastRecord != null) {
                // 2. 如果存在，则更新它（累加重试次数）
                lastRecord.setRetryCount(lastRecord.getRetryCount() + 1);
                lastRecord.setStatus(0); // 重置为待重试，等待 Job 再次捞取
                lastRecord.setNextRetryTime(new Date(System.currentTimeMillis() + 5 * 60 * 1000));
                lastRecord.setFailReason("重试失败，进入死信队列等待下次调度");

                failedMailRecordMapper.updateById(lastRecord);
                log.info("✅ 更新失败记录，recordId={}, 当前累计重试次数: {}", lastRecord.getId(), lastRecord.getRetryCount());
            } else {
                // 3. 如果是第一次失败（理论上应该走 MailConsumer 的直接入库逻辑，但为了兜底）
                FailedMailRecord record = new FailedMailRecord();
                record.setRecipient(mailMessage.getTo());
                record.setSubject(mailMessage.getSubject());
                record.setContent(mailMessage.getContent());
                record.setFailReason("邮件发送失败，进入死信队列");
                record.setStatus(0);
                record.setRetryCount(1); // 第一次记为 1
                record.setNextRetryTime(new Date(System.currentTimeMillis() + 5 * 60 * 1000));

                failedMailRecordMapper.insert(record);
                log.info("✅ 新增失败记录，recordId={}", record.getId());
            }
        } catch (Exception e) {
            log.error("❌ 保存失败记录到数据库失败：{}", e.getMessage(), e);
        }
    }
}
