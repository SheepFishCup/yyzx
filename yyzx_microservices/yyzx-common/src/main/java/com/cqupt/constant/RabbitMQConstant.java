package com.cqupt.constant;

/**
 * RabbitMQ 队列/交换机/路由键常量
 * 供 yyzx-common (RabbitMQProducerService) 和 yyzx-notification (RabbitMQConfig) 共同引用
 */
public class RabbitMQConstant {

    // ========== 队列名称 ==========
    public static final String MAIL_QUEUE = "yyzx.mail.queue";
    public static final String NOTIFY_QUEUE = "yyzx.notify.queue";
    public static final String LOG_QUEUE = "yyzx.log.queue";
    public static final String ADMIN_NOTIFY_QUEUE = "yyzx.admin.notify.queue";

    // ========== 死信队列 ==========
    public static final String MAIL_DLX_QUEUE = "yyzx.mail.dlx.queue";
    public static final String NOTIFY_DLX_QUEUE = "yyzx.notify.dlx.queue";
    public static final String LOG_DLX_QUEUE = "yyzx.log.dlx.queue";
    public static final String ADMIN_NOTIFY_DLX_QUEUE = "yyzx.admin.notify.dlx.queue";

    // ========== 交换机 ==========
    public static final String EXCHANGE_NAME = "yyzx.direct.exchange";
    public static final String DLX_EXCHANGE_NAME = "yyzx.dlx.exchange";

    // ========== 路由键 ==========
    public static final String MAIL_ROUTING_KEY = "yyzx.mail";
    public static final String NOTIFY_ROUTING_KEY = "yyzx.notify";
    public static final String LOG_ROUTING_KEY = "yyzx.log";
    public static final String ADMIN_NOTIFY_ROUTING_KEY = "yyzx.admin.notify";

    // ========== 死信路由键 ==========
    public static final String MAIL_DLX_ROUTING_KEY = "yyzx.mail.dlx";
    public static final String NOTIFY_DLX_ROUTING_KEY = "yyzx.notify.dlx";
    public static final String LOG_DLX_ROUTING_KEY = "yyzx.log.dlx";
    public static final String ADMIN_NOTIFY_DLX_ROUTING_KEY = "yyzx.admin.notify.dlx";

    private RabbitMQConstant() {}
}
