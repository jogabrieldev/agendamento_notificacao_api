package com.java.agendamento_notificacao_api.business.provider;

import com.java.agendamento_notificacao_api.infrastructure.entities.EntregaNotificacao;
import com.java.agendamento_notificacao_api.infrastructure.enums.CanalNotificacaoEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "notification.provider.mode",
        havingValue = "simulation",
        matchIfMissing = true
)
public class EmailNotificacaoSender implements NotificacaoSender {

    @Override
    public CanalNotificacaoEnum canal() {
        return CanalNotificacaoEnum.EMAIL;
    }

    @Override
    public String enviar(EntregaNotificacao entrega) {
        String providerMessageId = "email-simulated-" + UUID.randomUUID();
        log.info("E-mail simulado: destinatario={}, providerMessageId={}",
                entrega.getDestinatario(), providerMessageId);
        return providerMessageId;
    }
}
