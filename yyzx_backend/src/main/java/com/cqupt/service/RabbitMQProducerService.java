package com.cqupt.service;

import com.cqupt.config.RabbitMQConfig;
import com.cqupt.dto.LogMessage;
import com.cqupt.dto.MailMessage;
import com.cqupt.dto.NotifyMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMQProducerService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMail(MailMessage mailMessage) {
        log.info("发送邮件消息到队列：{}", mailMessage.getTo());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, 
                RabbitMQConfig.MAIL_ROUTING_KEY, mailMessage);
    }

    public void sendNotify(NotifyMessage notifyMessage) {
        log.info("发送通知消息到队列：userId={}, type={}", 
                notifyMessage.getUserId(), notifyMessage.getType());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, 
                RabbitMQConfig.NOTIFY_ROUTING_KEY, notifyMessage);
    }

    public void sendLog(LogMessage logMessage) {
        log.debug("发送日志消息到队列：module={}, action={}", 
                logMessage.getModule(), logMessage.getAction());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, 
                RabbitMQConfig.LOG_ROUTING_KEY, logMessage);
    }
    /**
     * 发送管理员群组通知
     */
    public void sendAdminNotify(NotifyMessage notifyMessage) {
        log.info("发送管理员群组通知：title={}, type={}",
                notifyMessage.getTitle(), notifyMessage.getType());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ADMIN_NOTIFY_ROUTING_KEY, notifyMessage);
    }
}
