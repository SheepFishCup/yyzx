package com.cqupt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqupt.dto.MailMessage;
import com.cqupt.mapper.FailedMailRecordMapper;
import com.cqupt.pojo.FailedMailRecord;
import com.cqupt.service.FailedMailService;
import com.cqupt.service.MailService;
import com.cqupt.service.RabbitMQProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class FailedMailServiceImpl implements FailedMailService {

    @Autowired
    private FailedMailRecordMapper failedMailRecordMapper;

    @Autowired
    private MailService mailService;

    @Autowired
    private RabbitMQProducerService rabbitMQProducerService;

    private static final int MAX_RETRY_COUNT = 3;

    @Override
    public List<FailedMailRecord> findDueRecords() {
        QueryWrapper<FailedMailRecord> qw = new QueryWrapper<>();
        qw.eq("status", 0);
        qw.and(wrapper -> wrapper
                .isNull("next_retry_time")
                .or()
                .le("next_retry_time", new Date())
        );
        qw.orderByAsc("create_time");
        qw.last("LIMIT 50");
        
        return failedMailRecordMapper.selectList(qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryMail(FailedMailRecord record) throws Exception {
        log.info("🔄 开始重试失败邮件：recordId={}, to={}, subject={}",
                record.getId(), record.getRecipient(), record.getSubject());

//        record.setStatus(1);
//        record.setRetryCount(record.getRetryCount() + 1);
//        failedMailRecordMapper.updateById(record);

        try {
            MailMessage mailMessage = MailMessage.builder()
                    .to(record.getRecipient())
                    .subject(record.getSubject())
                    .content(record.getContent())
                    .build();

            rabbitMQProducerService.sendMail(mailMessage);

            log.info("✅ 失败邮件已重新发送到队列：recordId={}", record.getId());
//            record.setStatus(2);
//            record.setNextRetryTime(null);
//            failedMailRecordMapper.updateById(record);

        } catch (Exception e) {
            log.error("❌ 重试失败：recordId={}, error={}", record.getId(), e.getMessage(), e);

            if (record.getRetryCount() >= MAX_RETRY_COUNT) {
                markAsFailed(record.getId(), "超过最大重试次数：" + e.getMessage());
            } else {
                record.setStatus(0);
                record.setNextRetryTime(new Date(System.currentTimeMillis() + 10 * 60 * 1000));
                failedMailRecordMapper.updateById(record);
            }
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsSuccess(Long id) {
        FailedMailRecord record = failedMailRecordMapper.selectById(id);
        if (record != null) {
            record.setStatus(2);
            record.setNextRetryTime(null);
            failedMailRecordMapper.updateById(record);
            log.info("✅ 标记为成功：recordId={}", id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsFailed(Long id, String reason) {
        FailedMailRecord record = failedMailRecordMapper.selectById(id);
        if (record != null) {
            record.setStatus(3);
            record.setFailReason(reason);
            record.setNextRetryTime(null);
            failedMailRecordMapper.updateById(record);
            log.warn("⚠️ 标记为失败：recordId={}, reason={}", id, reason);
        }
    }
}
