package com.java.agendamento_notificacao_api.controller.out;

import com.java.agendamento_notificacao_api.infrastructure.enums.StatusNotificacaoEnum;

import java.time.OffsetDateTime;

public record AgendamentoResumoResponse(
        Long id,
        String mensagem,
        StatusNotificacaoEnum status,
        OffsetDateTime dataHoraEnvio,
        int quantidadeEntregas
) {
}
