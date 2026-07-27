package com.cqupt.service.consumer;

import com.alibaba.fastjson.JSON;
import com.cqupt.config.RabbitMQConfig;
import com.cqupt.dto.MailMessage;
import com.cqupt.mapper.UserMapper;
import com.cqupt.pojo.User;
import com.cqupt.service.MailService;
import com.cqupt.websocket.WebSocketServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MailConsumer {

    @Autowired
    private MailService mailService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitMQConfig.MAIL_QUEUE)
    public void consumeMail(MailMessage mailMessage, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            log.info("接收到邮件发送任务：to={}, subject={}",
                    mailMessage.getTo(), mailMessage.getSubject());

            if (mailMessage.getSubject().contains("密码重置")) {
                String[] parts = mailMessage.getContent().split("\\|");
                String username = parts[0];
                String resetUrl = parts[1];

                mailService.sendPasswordResetEmail(
                        mailMessage.getTo(),
                        resetUrl,
                        username
                );

                log.info("✅ 重置密码邮件发送成功：to={}, username={}",
                        mailMessage.getTo(), username);

                sendPasswordResetNotify(mailMessage.getTo(), username);
            } else {
                // 异步发送普通邮件
                mailService.sendSimpleMail(
                        mailMessage.getTo(),
                        mailMessage.getSubject(),
                        mailMessage.getContent()
                );
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("❌ 邮件发送失败：to={}, error={}",
                    mailMessage.getTo(), e.getMessage(), e);

            try {
                // ❌ 关键修改：basicNack 第三个参数为 false，不重新入队，进入死信队列
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("消息拒绝失败：{}", ex.getMessage());
            }
        }
    }

    /**
     * 发送密码重置通知到 WebSocket
     */
    private void sendPasswordResetNotify(String email, String username) {
        try {
            // 根据邮箱查询用户
            User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                    .eq("email", email)
                    .eq("is_deleted", 0));

            if (user != null) {
                Map<String, Object> message = new HashMap<>();
                message.put("type", 3); // 3 表示密码重置邮件已发送
                message.put("userId", user.getId());
                message.put("title", "密码重置邮件已发送");
                message.put("content", "您的密码重置邮件已发送至邮箱：" + email + "，请及时查收并重置密码。");

                webSocketServer.sendToUser(user.getId().toString(), JSON.toJSONString(message));

                log.info("✅ WebSocket 密码重置通知已发送：userId={}, email={}", user.getId(), email);
            } else {
                log.warn("⚠️ 未找到对应的用户：email={}", email);
            }
        } catch (Exception e) {
            log.error("❌ 发送 WebSocket 通知失败：email={}, error={}", email, e.getMessage());
        }
    }


    /**
     * 发送简单文本邮件
     */
    private void sendSimpleMail(MailMessage mailMessage) {
        mailService.sendSimpleMail(
                mailMessage.getTo(),
                mailMessage.getSubject(),
                mailMessage.getContent()
        );

        log.info("✅ 简单邮件发送成功：to={}", mailMessage.getTo());
    }
}
