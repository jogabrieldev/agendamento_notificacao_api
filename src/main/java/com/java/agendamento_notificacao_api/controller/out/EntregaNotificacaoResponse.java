package com.java.agendamento_notificacao_api.controller.out;

import com.java.agendamento_notificacao_api.infrastructure.enums.CanalNotificacaoEnum;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusEntregaEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(name = "EntregaNotificacaoResponse", description = "Situação do envio em um canal")
public record EntregaNotificacaoResponse(
        Long id,
        CanalNotificacaoEnum canal,
        String destinatario,
        StatusEntregaEnum status,
        Integer quantidadeTentativas,
        String providerMessageId,
        String ultimoErro,
        OffsetDateTime processadaEm,
        OffsetDateTime enviadaEm
) {
}
