package com.cqupt.config;

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

@Configuration
public class RabbitMQConfig {
    // 队列名称常量
    public static final String MAIL_QUEUE = "yyzx.mail.queue";
    public static final String NOTIFY_QUEUE = "yyzx.notify.queue";
    public static final String LOG_QUEUE = "yyzx.log.queue";
    public static final String ADMIN_NOTIFY_QUEUE = "yyzx.admin.notify.queue";
    // 死信队列
    public static final String MAIL_DLX_QUEUE = "yyzx.mail.dlx.queue";
    public static final String NOTIFY_DLX_QUEUE = "yyzx.notify.dlx.queue";
    public static final String LOG_DLX_QUEUE = "yyzx.log.dlx.queue";
    public static final String ADMIN_NOTIFY_DLX_QUEUE = "yyzx.admin.notify.dlx.queue";

    // 死信交换机
    public static final String DLX_EXCHANGE_NAME = "yyzx.dlx.exchange";

    // 交换机
    public static final String EXCHANGE_NAME = "yyzx.direct.exchange";
    // 路由键
    public static final String MAIL_ROUTING_KEY = "yyzx.mail";
    public static final String NOTIFY_ROUTING_KEY = "yyzx.notify";
    public static final String LOG_ROUTING_KEY = "yyzx.log";
    public static final String ADMIN_NOTIFY_ROUTING_KEY = "yyzx.admin.notify";
    // 死信路由键
    public static final String MAIL_DLX_ROUTING_KEY = "yyzx.mail.dlx";
    public static final String NOTIFY_DLX_ROUTING_KEY = "yyzx.notify.dlx";
    public static final String LOG_DLX_ROUTING_KEY = "yyzx.log.dlx";
    public static final String ADMIN_NOTIFY_DLX_ROUTING_KEY = "yyzx.admin.notify.dlx";

    @Bean
    public Queue mailQueue() {
        Map<String, Object> args = new HashMap<>();
        // 设置死信队列
        args.put("x-dead-letter-exchange", DLX_EXCHANGE_NAME);
        // 设置死信路由键
        args.put("x-dead-letter-routing-key", MAIL_DLX_ROUTING_KEY);
        args.put("x-message-ttl", 300000); // 5 分钟过期
        return new Queue(MAIL_QUEUE, true, false, false, args);
    }
    @Bean
    public Queue notifyQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key", NOTIFY_DLX_ROUTING_KEY);
        return new Queue(NOTIFY_QUEUE, true, false, false, args);
    }
    @Bean
    public Queue logQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key", LOG_DLX_ROUTING_KEY);
        return new Queue(LOG_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue adminNotifyQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE_NAME);
        args.put("x-dead-letter-routing-key", ADMIN_NOTIFY_DLX_ROUTING_KEY);
        return new Queue(ADMIN_NOTIFY_QUEUE, true, false, false, args);
    }
    // 死信队列
    @Bean
    public Queue mailDlxQueue() {
        return new Queue(MAIL_DLX_QUEUE, true);
    }
    @Bean
    public Queue notifyDlxQueue() {
        return new Queue(NOTIFY_DLX_QUEUE, true);
    }

    @Bean
    public Queue logDlxQueue() {
        return new Queue(LOG_DLX_QUEUE, true);
    }

    @Bean
    public Queue adminNotifyDlxQueue() {
        return new Queue(ADMIN_NOTIFY_DLX_QUEUE, true);
    }
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Binding mailBinding(Queue mailQueue, DirectExchange exchange) {
        return BindingBuilder.bind(mailQueue).to(exchange).with(MAIL_ROUTING_KEY);
    }

    @Bean
    public Binding notifyBinding(Queue notifyQueue, DirectExchange exchange) {
        return BindingBuilder.bind(notifyQueue).to(exchange).with(NOTIFY_ROUTING_KEY);
    }

    @Bean
    public Binding logBinding(Queue logQueue, DirectExchange exchange) {
        return BindingBuilder.bind(logQueue).to(exchange).with(LOG_ROUTING_KEY);
    }

    @Bean
    public Binding adminNotifyBinding(Queue adminNotifyQueue, DirectExchange exchange) {
        return BindingBuilder.bind(adminNotifyQueue).to(exchange).with(ADMIN_NOTIFY_ROUTING_KEY);
    }
    // 死信队列绑定
    @Bean
    public Binding mailDlxBinding(Queue mailDlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(mailDlxQueue).to(dlxExchange).with(MAIL_DLX_ROUTING_KEY);
    }

    @Bean
    public Binding notifyDlxBinding(Queue notifyDlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(notifyDlxQueue).to(dlxExchange).with(NOTIFY_DLX_ROUTING_KEY);
    }

    @Bean
    public Binding logDlxBinding(Queue logDlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(logDlxQueue).to(dlxExchange).with(LOG_DLX_ROUTING_KEY);
    }

    @Bean
    public Binding adminNotifyDlxBinding(Queue adminNotifyDlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(adminNotifyDlxQueue).to(dlxExchange).with(ADMIN_NOTIFY_DLX_ROUTING_KEY);
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
