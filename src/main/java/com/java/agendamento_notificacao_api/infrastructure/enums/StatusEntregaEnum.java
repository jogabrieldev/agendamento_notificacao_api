package com.java.agendamento_notificacao_api.infrastructure.enums;

public enum StatusEntregaEnum {
    PENDENTE,
    EM_FILA,
    PROCESSANDO,
    ENVIADA,
    AGUARDANDO_RETRY,
    FALHA_DEFINITIVA,
    CANCELADA
}
