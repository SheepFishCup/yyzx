package com.cqupt.service.consumer;

import com.cqupt.config.RabbitMQConfig;
import com.cqupt.dto.NotifyMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class NotifyConsumer {

    @RabbitListener(queues = RabbitMQConfig.NOTIFY_QUEUE)
    public void consumeNotify(NotifyMessage notifyMessage, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            log.info("接收到通知消息：userId={}, type={}, title={}", 
                    notifyMessage.getUserId(), 
                    notifyMessage.getType(), 
                    notifyMessage.getTitle());
            
            switch (notifyMessage.getType()) {
                case "WECHAT":
                    sendWechatNotify(notifyMessage);
                    break;
                case "SMS":
                    sendSmsNotify(notifyMessage);
                    break;
                case "SYSTEM":
                    saveSystemNotify(notifyMessage);
                    break;
                default:
                    log.warn("未知的通知类型：{}", notifyMessage.getType());
            }
            
            channel.basicAck(deliveryTag, false);
            log.info("通知处理成功：userId={}", notifyMessage.getUserId());
            
        } catch (Exception e) {
            log.error("通知处理失败：userId={}, error={}", 
                    notifyMessage.getUserId(), e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("消息拒绝失败：{}", ex.getMessage());
            }
        }
    }

    private void sendWechatNotify(NotifyMessage notifyMessage) {
        log.info("发送微信通知：userId={}", notifyMessage.getUserId());
    }

    private void sendSmsNotify(NotifyMessage notifyMessage) {
        log.info("发送短信通知：userId={}", notifyMessage.getUserId());
    }

    private void saveSystemNotify(NotifyMessage notifyMessage) {
        log.info("保存系统通知：userId={}", notifyMessage.getUserId());
    }
}
