package com.java.agendamento_notificacao_api.infrastructure.exception;

public class DataAgendamentoInvalidaException extends RuntimeException {
    public DataAgendamentoInvalidaException(String message) {
        super(message);
    }
}
