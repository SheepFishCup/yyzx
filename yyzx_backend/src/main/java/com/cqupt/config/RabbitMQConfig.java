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

@Configuration
public class RabbitMQConfig {
    // 队列名称常量
    public static final String MAIL_QUEUE = "yyzx.mail.queue";
    public static final String NOTIFY_QUEUE = "yyzx.notify.queue";
    public static final String LOG_QUEUE = "yyzx.log.queue";
    public static final String ADMIN_NOTIFY_QUEUE = "yyzx.admin.notify.queue";
    // 交换机
    public static final String EXCHANGE_NAME = "yyzx.direct.exchange";
    // 路由键
    public static final String MAIL_ROUTING_KEY = "yyzx.mail";
    public static final String NOTIFY_ROUTING_KEY = "yyzx.notify";
    public static final String LOG_ROUTING_KEY = "yyzx.log";
    public static final String ADMIN_NOTIFY_ROUTING_KEY = "yyzx.admin.notify";


    @Bean
    public Queue mailQueue() {
        return new Queue(MAIL_QUEUE, true);
    }

    @Bean
    public Queue notifyQueue() {
        return new Queue(NOTIFY_QUEUE, true);
    }

    @Bean
    public Queue logQueue() {
        return new Queue(LOG_QUEUE, true);
    }
    @Bean
    public Queue adminNotifyQueue() {
        return new Queue(ADMIN_NOTIFY_QUEUE, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
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
