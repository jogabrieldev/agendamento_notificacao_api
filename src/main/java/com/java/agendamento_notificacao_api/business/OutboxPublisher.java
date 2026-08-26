package com.java.agendamento_notificacao_api.business;

import com.java.agendamento_notificacao_api.infrastructure.config.RabbitMqConstants;
import com.java.agendamento_notificacao_api.infrastructure.entities.OutboxEvent;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusEntregaEnum;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusOutboxEnum;
import com.java.agendamento_notificacao_api.infrastructure.repositories.OutboxEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final int LIMITE_TENTATIVAS = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    @Value("${notification.outbox.batch-size:50}")
    private int batchSize;

    @Value("${notification.outbox.confirm-timeout-seconds:5}")
    private long confirmTimeoutSeconds;

    @Transactional
    @Scheduled(fixedDelayString = "${notification.outbox.fixed-delay-ms:1000}")
    public void publicarEventosPendentes() {
        List<OutboxEvent> eventos = outboxEventRepository.buscarPendentesParaPublicacao(
                StatusOutboxEnum.PENDENTE,
                clock.instant(),
                PageRequest.of(0, batchSize)
        );

        eventos.forEach(this::publicar);
    }

    private void publicar(OutboxEvent evento) {
        try {
            CorrelationData correlationData = new CorrelationData(evento.getId().toString());
            rabbitTemplate.convertAndSend(
                    RabbitMqConstants.EXCHANGE,
                    evento.getRoutingKey(),
                    evento.getPayload(),
                    message -> {
                        message.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
                        message.getMessageProperties().setMessageId(evento.getId().toString());
                        message.getMessageProperties().setHeader("eventType", evento.getTipoEvento());
                        return message;
                    },
                    correlationData
            );

            CorrelationData.Confirm confirmacao = correlationData.getFuture()
                    .get(confirmTimeoutSeconds, TimeUnit.SECONDS);
            if (!confirmacao.isAck()) {
                throw new IllegalStateException("RabbitMQ recusou a publicação: " + confirmacao.getReason());
            }
            if (correlationData.getReturned() != null) {
                throw new IllegalStateException("Mensagem não foi roteada para uma fila");
            }

            evento.setStatus(StatusOutboxEnum.PUBLICADO);
            evento.setPublicadoEm(clock.instant());
            evento.setUltimoErro(null);
            evento.getEntrega().setStatus(StatusEntregaEnum.EM_FILA);
            log.info("Evento outbox publicado: eventoId={}, routingKey={}",
                    evento.getId(), evento.getRoutingKey());
        } catch (Exception exception) {
            registrarFalha(evento, exception);
        }
    }

    private void registrarFalha(OutboxEvent evento, Exception exception) {
        int tentativas = evento.getQuantidadeTentativas() + 1;
        evento.setQuantidadeTentativas(tentativas);
        evento.setUltimoErro(exception.getMessage());
        if (tentativas >= LIMITE_TENTATIVAS) {
            evento.setStatus(StatusOutboxEnum.FALHA);
        }
        log.error("Falha ao publicar evento outbox: eventoId={}, tentativa={}",
                evento.getId(), tentativas, exception);
    }
}
