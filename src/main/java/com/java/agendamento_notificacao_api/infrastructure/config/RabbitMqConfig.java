package com.java.agendamento_notificacao_api.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RabbitMqConfig {

    @Bean
    DirectExchange notificationExchange() {
        return new DirectExchange(RabbitMqConstants.EXCHANGE, true, false);
    }

    @Bean
    DirectExchange notificationDeadLetterExchange() {
        return new DirectExchange(RabbitMqConstants.DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    Queue emailQueue() {
        return QueueBuilder.durable(RabbitMqConstants.EMAIL_QUEUE)
                .deadLetterExchange(RabbitMqConstants.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMqConstants.EMAIL_DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    Queue smsQueue() {
        return QueueBuilder.durable(RabbitMqConstants.SMS_QUEUE)
                .deadLetterExchange(RabbitMqConstants.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMqConstants.SMS_DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    Queue emailDeadLetterQueue() {
        return QueueBuilder.durable(RabbitMqConstants.EMAIL_DLQ).build();
    }

    @Bean
    Queue smsDeadLetterQueue() {
        return QueueBuilder.durable(RabbitMqConstants.SMS_DLQ).build();
    }

    @Bean
    Binding emailBinding(
            @Qualifier("emailQueue") Queue emailQueue,
            @Qualifier("notificationExchange") DirectExchange notificationExchange
    ) {
        return BindingBuilder.bind(emailQueue)
                .to(notificationExchange)
                .with(RabbitMqConstants.EMAIL_ROUTING_KEY);
    }

    @Bean
    Binding smsBinding(
            @Qualifier("smsQueue") Queue smsQueue,
            @Qualifier("notificationExchange") DirectExchange notificationExchange
    ) {
        return BindingBuilder.bind(smsQueue)
                .to(notificationExchange)
                .with(RabbitMqConstants.SMS_ROUTING_KEY);
    }

    @Bean
    Binding emailDeadLetterBinding(
            @Qualifier("emailDeadLetterQueue") Queue emailDeadLetterQueue,
            @Qualifier("notificationDeadLetterExchange") DirectExchange notificationDeadLetterExchange
    ) {
        return BindingBuilder.bind(emailDeadLetterQueue)
                .to(notificationDeadLetterExchange)
                .with(RabbitMqConstants.EMAIL_DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    Binding smsDeadLetterBinding(
            @Qualifier("smsDeadLetterQueue") Queue smsDeadLetterQueue,
            @Qualifier("notificationDeadLetterExchange") DirectExchange notificationDeadLetterExchange
    ) {
        return BindingBuilder.bind(smsDeadLetterQueue)
                .to(notificationDeadLetterExchange)
                .with(RabbitMqConstants.SMS_DEAD_LETTER_ROUTING_KEY);
    }
}
