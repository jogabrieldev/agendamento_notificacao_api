package com.java.agendamento_notificacao_api.business.provider.real;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "notification.provider.real")
public record RealProviderProperties(
        @NotNull @Valid Resend resend,
        @NotNull @Valid Twilio twilio
) {
    public record Resend(
            @NotBlank String apiKey,
            @NotBlank String from,
            @NotBlank String subject
    ) {
    }

    public record Twilio(
            @NotBlank String accountSid,
            @NotBlank String authToken,
            @NotBlank String from
    ) {
    }
}
