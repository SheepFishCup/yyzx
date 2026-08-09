package com.cqupt.notification.service;

/**
 * 钉钉机器人服务接口
 */
public interface DingTalkRobotService {

    /** 发送文本消息到钉钉群 */
    void sendTextMessage(String content);

    /** 发送 Markdown 消息到钉钉群 */
    void sendMarkdownMessage(String title, String text);
}
