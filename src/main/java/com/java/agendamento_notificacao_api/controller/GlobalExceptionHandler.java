package com.java.agendamento_notificacao_api.controller;

import com.java.agendamento_notificacao_api.controller.out.ApiErrorResponse;
import com.java.agendamento_notificacao_api.infrastructure.config.TimeConfig;
import com.java.agendamento_notificacao_api.infrastructure.exception.DataAgendamentoInvalidaException;
import com.java.agendamento_notificacao_api.infrastructure.exception.NotFoudException;
import com.java.agendamento_notificacao_api.infrastructure.exception.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Clock clock;

    @ExceptionHandler(NotFoudException.class)
    public ResponseEntity<ApiErrorResponse> tratarNaoEncontrado(
            NotFoudException exception,
            HttpServletRequest request
    ) {
        return resposta(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ApiErrorResponse> tratarConflito(
            RegraNegocioException exception,
            HttpServletRequest request
    ) {
        return resposta(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(DataAgendamentoInvalidaException.class)
    public ResponseEntity<ApiErrorResponse> tratarDataAgendamentoInvalida(
            DataAgendamentoInvalidaException exception,
            HttpServletRequest request
    ) {
        return resposta(HttpStatus.BAD_REQUEST, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> tratarValidacaoBody(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> campos = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        exception.getBindingResult().getGlobalErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getObjectName(), erro.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Dados da requisição são inválidos", request, campos);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> tratarValidacaoParametros(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> campos = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violacao ->
                campos.put(violacao.getPropertyPath().toString(), violacao.getMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetros da requisição são inválidos", request, campos);
    }

    private ResponseEntity<ApiErrorResponse> resposta(
            HttpStatus status,
            String mensagem,
            HttpServletRequest request,
            Map<String, String> campos
    ) {
        ApiErrorResponse erro = new ApiErrorResponse(
                clock.instant().atZone(TimeConfig.BRAZIL_ZONE).toOffsetDateTime(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI(),
                campos
        );
        return ResponseEntity.status(status).body(erro);
    }
}
