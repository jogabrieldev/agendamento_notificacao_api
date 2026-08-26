package com.java.agendamento_notificacao_api.controller.dto;

import java.time.OffsetDateTime;

public record AgendamentoRecord(String emailDestinatario, String telefoneDestinatario, String mensagem,
                                OffsetDateTime dataHoraEnvio) {
}
