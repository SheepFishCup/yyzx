package com.cqupt.notification.consumer;

import com.cqupt.notification.config.RabbitMQConfig;
import com.cqupt.dto.LogMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class LogConsumer {

    @RabbitListener(queues = RabbitMQConfig.LOG_QUEUE)
    public void consumeLog(LogMessage logMessage, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            log.info("接收到日志消息：level={}, module={}, action={}", 
                    logMessage.getLevel(), 
                    logMessage.getModule(), 
                    logMessage.getAction());
            
            saveLogToDatabase(logMessage);
            
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            log.error("日志处理失败：module={}, error={}", 
                    logMessage.getModule(), e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("消息拒绝失败：{}", ex.getMessage());
            }
        }
    }

    private void saveLogToDatabase(LogMessage logMessage) {
        log.info("保存日志到数据库：{} - {} - {}", 
                logMessage.getModule(), 
                logMessage.getAction(), 
                logMessage.getMessage());
    }
}
