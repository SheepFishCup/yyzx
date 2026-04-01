package com.cqupt.service.consumer;

import com.cqupt.config.RabbitMQConfig;
import com.cqupt.dto.MailMessage;
import com.cqupt.service.MailService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class MailConsumer {

    @Autowired
    private MailService mailService;

    @RabbitListener(queues = RabbitMQConfig.MAIL_QUEUE)
    public void consumeMail(MailMessage mailMessage, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            log.info("接收到邮件消息：to={}, subject={}", 
                    mailMessage.getTo(), mailMessage.getSubject());
            
            if (mailMessage.getIsHtml()) {
                mailService.sendHtmlMail(mailMessage.getTo(), 
                        mailMessage.getSubject(), mailMessage.getContent());
            } else {
                mailService.sendSimpleMail(mailMessage.getTo(), 
                        mailMessage.getSubject(), mailMessage.getContent());
            }
            
            channel.basicAck(deliveryTag, false);
            log.info("邮件发送成功：{}", mailMessage.getTo());
            
        } catch (Exception e) {
            log.error("邮件发送失败：to={}, error={}", mailMessage.getTo(), e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("消息拒绝失败：{}", ex.getMessage());
            }
        }
    }
}
