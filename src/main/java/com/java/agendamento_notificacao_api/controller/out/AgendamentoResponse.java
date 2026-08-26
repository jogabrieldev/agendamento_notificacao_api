package com.java.agendamento_notificacao_api.controller.out;

import com.java.agendamento_notificacao_api.infrastructure.enums.StatusNotificacaoEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(name = "AgendamentoResponse", description = "Detalhes e progresso do agendamento")
public record AgendamentoResponse(
        Long id,
        String mensagem,
        StatusNotificacaoEnum status,
        OffsetDateTime dataHoraEnvio,
        OffsetDateTime dataHoraAgendamento,
        OffsetDateTime dataHoraModificacao,
        List<EntregaNotificacaoResponse> entregas
) {
}
