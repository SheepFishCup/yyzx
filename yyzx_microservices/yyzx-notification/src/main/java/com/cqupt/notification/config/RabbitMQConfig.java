package com.cqupt.notification.config;

import com.cqupt.constant.RabbitMQConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 配置 — 声明所有队列/交换机/绑定
 * 队列名称/路由键等常量统一引用 common 模块的 RabbitMQConstant
 */
@Configuration
public class RabbitMQConfig {

    // ========== 常量重导出（兼容 @RabbitListener 注解引用） ==========
    public static final String MAIL_QUEUE = RabbitMQConstant.MAIL_QUEUE;
    public static final String NOTIFY_QUEUE = RabbitMQConstant.NOTIFY_QUEUE;
    public static final String LOG_QUEUE = RabbitMQConstant.LOG_QUEUE;
    public static final String ADMIN_NOTIFY_QUEUE = RabbitMQConstant.ADMIN_NOTIFY_QUEUE;
    public static final String MAIL_DLX_QUEUE = RabbitMQConstant.MAIL_DLX_QUEUE;
    public static final String NOTIFY_DLX_QUEUE = RabbitMQConstant.NOTIFY_DLX_QUEUE;
    public static final String LOG_DLX_QUEUE = RabbitMQConstant.LOG_DLX_QUEUE;
    public static final String ADMIN_NOTIFY_DLX_QUEUE = RabbitMQConstant.ADMIN_NOTIFY_DLX_QUEUE;
    public static final String DLX_EXCHANGE_NAME = RabbitMQConstant.DLX_EXCHANGE_NAME;
    public static final String EXCHANGE_NAME = RabbitMQConstant.EXCHANGE_NAME;
    public static final String MAIL_ROUTING_KEY = RabbitMQConstant.MAIL_ROUTING_KEY;
    public static final String NOTIFY_ROUTING_KEY = RabbitMQConstant.NOTIFY_ROUTING_KEY;
    public static final String LOG_ROUTING_KEY = RabbitMQConstant.LOG_ROUTING_KEY;
    public static final String ADMIN_NOTIFY_ROUTING_KEY = RabbitMQConstant.ADMIN_NOTIFY_ROUTING_KEY;
    public static final String MAIL_DLX_ROUTING_KEY = RabbitMQConstant.MAIL_DLX_ROUTING_KEY;
    public static final String NOTIFY_DLX_ROUTING_KEY = RabbitMQConstant.NOTIFY_DLX_ROUTING_KEY;
    public static final String LOG_DLX_ROUTING_KEY = RabbitMQConstant.LOG_DLX_ROUTING_KEY;
    public static final String ADMIN_NOTIFY_DLX_ROUTING_KEY = RabbitMQConstant.ADMIN_NOTIFY_DLX_ROUTING_KEY;

    @Bean
    public Queue mailQueue() {
        Map<String, Object> args = new HashMap<>();
        // 设置死信队列
        args.put("x-dead-letter-exchange", RabbitMQConstant.DLX_EXCHANGE_NAME);
        // 设置死信路由键
        args.put("x-dead-letter-routing-key", RabbitMQConstant.MAIL_DLX_ROUTING_KEY);
        args.put("x-message-ttl", 300000); // 5 分钟过期
        return new Queue(RabbitMQConstant.MAIL_QUEUE, true, false, false, args);
    }
    @Bean
    public Queue notifyQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", RabbitMQConstant.DLX_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key", RabbitMQConstant.NOTIFY_DLX_ROUTING_KEY);
        return new Queue(RabbitMQConstant.NOTIFY_QUEUE, true, false, false, args);
    }
    @Bean
    public Queue logQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", RabbitMQConstant.DLX_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key", RabbitMQConstant.LOG_DLX_ROUTING_KEY);
        return new Queue(RabbitMQConstant.LOG_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue adminNotifyQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", RabbitMQConstant.DLX_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key", RabbitMQConstant.ADMIN_NOTIFY_DLX_ROUTING_KEY);
        return new Queue(RabbitMQConstant.ADMIN_NOTIFY_QUEUE, true, false, false, args);
    }
    // 死信队列
    @Bean
    public Queue mailDlxQueue() {
        return new Queue(RabbitMQConstant.MAIL_DLX_QUEUE, true);
    }
    @Bean
    public Queue notifyDlxQueue() {
        return new Queue(RabbitMQConstant.NOTIFY_DLX_QUEUE, true);
    }

    @Bean
    public Queue logDlxQueue() {
        return new Queue(RabbitMQConstant.LOG_DLX_QUEUE, true);
    }

    @Bean
    public Queue adminNotifyDlxQueue() {
        return new Queue(RabbitMQConstant.ADMIN_NOTIFY_DLX_QUEUE, true);
    }
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(RabbitMQConstant.EXCHANGE_NAME, true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(RabbitMQConstant.DLX_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Binding mailBinding(Queue mailQueue, DirectExchange exchange) {
        return BindingBuilder.bind(mailQueue).to(exchange).with(RabbitMQConstant.MAIL_ROUTING_KEY);
    }

    @Bean
    public Binding notifyBinding(Queue notifyQueue, DirectExchange exchange) {
        return BindingBuilder.bind(notifyQueue).to(exchange).with(RabbitMQConstant.NOTIFY_ROUTING_KEY);
    }

    @Bean
    public Binding logBinding(Queue logQueue, DirectExchange exchange) {
        return BindingBuilder.bind(logQueue).to(exchange).with(RabbitMQConstant.LOG_ROUTING_KEY);
    }

    @Bean
    public Binding adminNotifyBinding(Queue adminNotifyQueue, DirectExchange exchange) {
        return BindingBuilder.bind(adminNotifyQueue).to(exchange).with(RabbitMQConstant.ADMIN_NOTIFY_ROUTING_KEY);
    }
    // 死信队列绑定
    @Bean
    public Binding mailDlxBinding(Queue mailDlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(mailDlxQueue).to(dlxExchange).with(RabbitMQConstant.MAIL_DLX_ROUTING_KEY);
    }

    @Bean
    public Binding notifyDlxBinding(Queue notifyDlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(notifyDlxQueue).to(dlxExchange).with(RabbitMQConstant.NOTIFY_DLX_ROUTING_KEY);
    }

    @Bean
    public Binding logDlxBinding(Queue logDlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(logDlxQueue).to(dlxExchange).with(RabbitMQConstant.LOG_DLX_ROUTING_KEY);
    }

    @Bean
    public Binding adminNotifyDlxBinding(Queue adminNotifyDlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(adminNotifyDlxQueue).to(dlxExchange).with(RabbitMQConstant.ADMIN_NOTIFY_DLX_ROUTING_KEY);
    }
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    // 创建RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }


}
