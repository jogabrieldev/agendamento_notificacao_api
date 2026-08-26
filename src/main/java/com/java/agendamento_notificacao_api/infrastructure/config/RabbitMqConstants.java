package com.java.agendamento_notificacao_api.infrastructure.config;

public final class RabbitMqConstants {

    public static final String EXCHANGE = "notification.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "notification.dlx";

    public static final String EMAIL_QUEUE = "notification.email.send";
    public static final String SMS_QUEUE = "notification.sms.send";
    public static final String EMAIL_DLQ = "notification.email.dlq";
    public static final String SMS_DLQ = "notification.sms.dlq";

    public static final String EMAIL_ROUTING_KEY = "notification.email";
    public static final String SMS_ROUTING_KEY = "notification.sms";
    public static final String EMAIL_DEAD_LETTER_ROUTING_KEY = "notification.email.dead";
    public static final String SMS_DEAD_LETTER_ROUTING_KEY = "notification.sms.dead";

    private RabbitMqConstants() {
    }
}
