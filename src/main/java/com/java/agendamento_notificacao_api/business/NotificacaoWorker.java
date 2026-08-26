package com.java.agendamento_notificacao_api.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.agendamento_notificacao_api.business.message.NotificacaoMessage;
import com.java.agendamento_notificacao_api.infrastructure.config.RabbitMqConstants;
import com.java.agendamento_notificacao_api.infrastructure.enums.CanalNotificacaoEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacaoWorker {

    private final ObjectMapper objectMapper;
    private final NotificacaoProcessor processor;
    private final FalhaEntregaService falhaEntregaService;

    @RabbitListener(queues = RabbitMqConstants.EMAIL_QUEUE)
    public void processarEmail(String payload) {
        processar(payload, CanalNotificacaoEnum.EMAIL);
    }

    @RabbitListener(queues = RabbitMqConstants.SMS_QUEUE)
    public void processarSms(String payload) {
        processar(payload, CanalNotificacaoEnum.SMS);
    }

    private void processar(String payload, CanalNotificacaoEnum canal) {
        NotificacaoMessage message = converter(payload);
        try {
            processor.processar(message, canal);
        } catch (RuntimeException exception) {
            falhaEntregaService.registrar(message.entregaId(), exception);
            throw exception;
        }
    }

    private NotificacaoMessage converter(String payload) {
        try {
            return objectMapper.readValue(payload, NotificacaoMessage.class);
        } catch (JsonProcessingException exception) {
            log.error("Mensagem inválida recebida na fila: payload={}", payload, exception);
            throw new IllegalArgumentException("Payload de notificação inválido", exception);
        }
    }
}
