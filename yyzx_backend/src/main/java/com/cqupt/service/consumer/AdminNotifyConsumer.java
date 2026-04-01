package com.cqupt.service.consumer;

import com.cqupt.config.RabbitMQConfig;
import com.cqupt.dto.NotifyMessage;
import com.cqupt.mapper.UserMapper;
import com.cqupt.pojo.User;
import com.cqupt.service.DingTalkRobotService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class AdminNotifyConsumer {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DingTalkRobotService dingTalkRobotService;

    @RabbitListener(queues = RabbitMQConfig.ADMIN_NOTIFY_QUEUE)
    public void consumeAdminNotify(NotifyMessage notifyMessage, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            log.info("接收到管理员群组通知：title={}, type={}",
                    notifyMessage.getTitle(), notifyMessage.getType());

            // 查询所有管理员（用于验证是否有管理员）
            List<User> adminList = getAllAdmins();

            if (adminList == null || adminList.isEmpty()) {
                log.warn("系统中没有管理员，无法发送通知");
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 只发送一次到钉钉群（所有管理员都能看到）
            sendToDingTalkGroup(notifyMessage);

            log.info("管理员群组通知发送完成：群内管理员人数={}", adminList.size());

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理管理员群组通知失败：error={}", e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("消息拒绝失败：{}", ex.getMessage());
            }
        }
    }

    /**
     * 查询所有管理员
     */
    private List<User> getAllAdmins() {
        // 假设 roleId=1 是管理员
        return userMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .eq("role_id", 1)
                .eq("is_deleted", 0));
    }

    /**
     * 发送通知到钉钉群（所有管理员共享一个群）
     */
    private void sendToDingTalkGroup(NotifyMessage notifyMessage) {
        try {
            String content = buildDingTalkContent(notifyMessage);
            dingTalkRobotService.sendTextMessage(content);
            log.info("✅ 钉钉群通知发送成功");
        } catch (Exception e) {
            log.error("❌ 发送钉钉群通知失败：error={}", e.getMessage(), e);
        }
    }

    /**
     * 发送给单个管理员（保留方法，暂时不用）
     */
    private void sendToAdmin(User admin, NotifyMessage notifyMessage) {
        log.info("发送通知给管理员：adminId={}, username={}, title={}",
                admin.getId(), admin.getUsername(), notifyMessage.getTitle());

        // 直接发送钉钉机器人通知
        sendDingTalkToAdmin(admin, notifyMessage);
    }

    /**
     * 发送微信通知给管理员
     */
    private void sendWechatToAdmin(User admin, NotifyMessage notifyMessage) {
        log.info("发送微信通知给管理员：adminId={}, content={}", 
                admin.getId(), notifyMessage.getContent());
        // TODO: 调用微信推送 API
    }

    /**
     * 发送短信通知给管理员
     */
    private void sendSmsToAdmin(User admin, NotifyMessage notifyMessage) {
        log.info("发送短信通知给管理员：adminId={}, content={}", 
                admin.getId(), notifyMessage.getContent());
        // TODO: 调用短信平台 API
    }

    /**
     * 保存系统通知到数据库
     */
    private void saveSystemNotify(User admin, NotifyMessage notifyMessage) {
        log.info("保存系统通知：adminId={}, title={}", admin.getId(), notifyMessage.getTitle());
        // TODO: 保存到通知表
    }
    /**
     * 发送钉钉机器人通知
     */
    private void sendDingTalkToAdmin(User admin, NotifyMessage notifyMessage) {
        try {
            String content = buildDingTalkContent(notifyMessage);
            dingTalkRobotService.sendTextMessage(content);
            log.info("✅ 钉钉机器人通知发送成功：adminId={}", admin.getId());
        } catch (Exception e) {
            log.error("❌ 发送钉钉机器人通知失败：adminId={}, error={}",
                    admin.getId(), e.getMessage());
        }
    }
    /**
     * 构建钉钉消息内容
     */
    private String buildDingTalkContent(NotifyMessage notifyMessage) {
        StringBuilder sb = new StringBuilder();

        // 添加标题 emoji
        switch (notifyMessage.getType()) {
            case "ALERT":
                sb.append("⚠️ ");
                break;
            case "WECHAT":
                sb.append("📢 ");
                break;
            case "SMS":
                sb.append("📱 ");
                break;
            default:
                sb.append("🔔 ");
                break;
        }

        sb.append("【").append(notifyMessage.getTitle()).append("】\n\n");
        sb.append(notifyMessage.getContent()).append("\n\n");
        sb.append("─────────────────────\n");
        sb.append("时间：").append(
                LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
        );

        return sb.toString();
    }
}
