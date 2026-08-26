package com.java.agendamento_notificacao_api.business.message;

import com.java.agendamento_notificacao_api.infrastructure.enums.CanalNotificacaoEnum;

public record NotificacaoMessage(
        Long agendamentoId,
        Long entregaId,
        CanalNotificacaoEnum canal
) {
}
