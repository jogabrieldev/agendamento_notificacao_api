package com.java.agendamento_notificacao_api.controller.out;

import com.java.agendamento_notificacao_api.infrastructure.enums.StatusNotificacaoEnum;

import java.time.OffsetDateTime;

public record AgendamentoRecordOut(Long id, String emailDestinatario, String telefoneDestinatario, String mensagem,
                                   OffsetDateTime dataHoraEnvio, StatusNotificacaoEnum statusNotificacao) {
}
