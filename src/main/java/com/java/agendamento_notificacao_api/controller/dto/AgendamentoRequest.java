package com.java.agendamento_notificacao_api.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

@Schema(name = "AgendamentoRequest", description = "Dados necessários para agendar uma notificação")
public record AgendamentoRequest(
        @Email(message = "E-mail do destinatário deve ser válido")
        @Pattern(
                regexp = "^(?!.*\\.\\.)[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}$",
                message = "E-mail deve possuir formato válido e domínio completo"
        )
        @Size(max = 320, message = "E-mail deve possuir no máximo 320 caracteres")
        @Schema(example = "cliente@example.com", nullable = true)
        String emailDestinatario,

        @Size(max = 20, message = "Telefone deve possuir no máximo 20 caracteres")
        @Pattern(
                regexp = "^\\+[1-9][0-9]{7,14}$",
                message = "Telefone deve estar no padrão internacional E.164, por exemplo +5511999999999"
        )
        @Schema(example = "+5511999999999", nullable = true)
        String telefoneDestinatario,

        @NotBlank(message = "Mensagem é obrigatória")
        @Size(max = 1000, message = "Mensagem deve possuir no máximo 1000 caracteres")
        @Schema(example = "Sua consulta começará em 30 minutos")
        String mensagem,

        @NotNull(message = "Data e hora de envio são obrigatórias")
        @Future(message = "Data e hora de envio devem estar no futuro")
        @Schema(example = "2026-12-30T14:30:00-03:00", type = "string")
        OffsetDateTime dataHoraEnvio
) {
    @AssertTrue(message = "Informe pelo menos um destinatário: e-mail ou telefone")
    @JsonIgnore
    @Schema(hidden = true)
    public boolean isDestinatarioInformado() {
        return possuiTexto(emailDestinatario) || possuiTexto(telefoneDestinatario);
    }

    private boolean possuiTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
