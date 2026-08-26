package com.java.agendamento_notificacao_api.controller.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.Map;

@Schema(name = "ApiErrorResponse", description = "Resposta padronizada de erro")
public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> campos
) {
}
