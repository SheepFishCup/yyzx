package com.cqupt.notification.service;

public interface MailService {
    /**
     * 发送简单邮件
     */
    void sendSimpleMail(String to, String subject, String content);

    /**
     * 发送HTML邮件
     */
    void sendHtmlMail(String to, String subject, String content);

    /**
     * 发送密码重置邮件
     */
    void sendPasswordResetEmail(String to, String resetUrl, String username);
}
