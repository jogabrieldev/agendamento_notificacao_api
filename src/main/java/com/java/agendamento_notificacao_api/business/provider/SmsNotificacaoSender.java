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
public class SmsNotificacaoSender implements NotificacaoSender {

    @Override
    public CanalNotificacaoEnum canal() {
        return CanalNotificacaoEnum.SMS;
    }

    @Override
    public String enviar(EntregaNotificacao entrega) {
        String providerMessageId = "sms-simulated-" + UUID.randomUUID();
        log.info("SMS simulado: destinatario={}, providerMessageId={}",
                entrega.getDestinatario(), providerMessageId);
        return providerMessageId;
    }
}
