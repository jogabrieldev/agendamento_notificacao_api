package com.java.agendamento_notificacao_api.business.provider;

import com.java.agendamento_notificacao_api.infrastructure.entities.EntregaNotificacao;
import com.java.agendamento_notificacao_api.infrastructure.enums.CanalNotificacaoEnum;

public interface NotificacaoSender {
    CanalNotificacaoEnum canal();

    String enviar(EntregaNotificacao entrega);
}
