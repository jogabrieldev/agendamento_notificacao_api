package com.java.agendamento_notificacao_api.controller;

import com.java.agendamento_notificacao_api.business.AgendamentoService;
import com.java.agendamento_notificacao_api.controller.dto.AgendamentoRequest;
import com.java.agendamento_notificacao_api.controller.out.AgendamentoResponse;
import com.java.agendamento_notificacao_api.controller.out.AgendamentoResumoResponse;
import com.java.agendamento_notificacao_api.controller.out.ApiErrorResponse;
import com.java.agendamento_notificacao_api.controller.out.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/agendamentos")
@Tag(name = "Agendamentos", description = "Agendamento e acompanhamento de notificações por e-mail e SMS")
public class AgendamentoController {
    private final AgendamentoService agendamentoService;

    @PostMapping
    @Operation(
            summary = "Criar agendamento",
            description = "Agenda uma notificação para um ou dois canais. A publicação no RabbitMQ ocorre somente no horário informado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agendamento criado"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<AgendamentoResponse> criar(
            @Valid @RequestBody AgendamentoRequest request
    ) {
        AgendamentoResponse response = agendamentoService.gravarAgendamento(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar agendamento",
            description = "Retorna o estado geral e o progresso individual das entregas de e-mail e SMS."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento encontrado"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Agendamento não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<AgendamentoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.buscarAgendamentoPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar agendamentos", description = "Lista os agendamentos do mais recente para o mais antigo.")
    @ApiResponse(responseCode = "200", description = "Página retornada")
    public ResponseEntity<PaginaResponse<AgendamentoResumoResponse>> listar(
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho
    ) {
        return ResponseEntity.ok(agendamentoService.listarAgendamentos(pagina, tamanho));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Cancelar agendamento",
            description = "Cancela entregas ainda não enviadas. Uma notificação completamente enviada não pode ser cancelada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Agendamento cancelado"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Agendamento não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Agendamento não pode mais ser cancelado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        agendamentoService.cancelarAgendamento(id);
        return ResponseEntity.noContent().build();
    }
}
