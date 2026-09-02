package com.java.agendamento_notificacao_api.business.provider.real;

import com.java.agendamento_notificacao_api.business.provider.NotificacaoSender;
import com.java.agendamento_notificacao_api.infrastructure.entities.EntregaNotificacao;
import com.java.agendamento_notificacao_api.infrastructure.enums.CanalNotificacaoEnum;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@ConditionalOnProperty(name = "notification.provider.mode", havingValue = "production")
public class ResendEmailNotificacaoSender implements NotificacaoSender {

    private final RestClient restClient;
    private final RealProviderProperties.Resend properties;

    public ResendEmailNotificacaoSender(RestClient.Builder builder, RealProviderProperties properties) {
        this.properties = properties.resend();
        this.restClient = builder
                .baseUrl("https://api.resend.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + this.properties.apiKey())
                .defaultHeader(HttpHeaders.USER_AGENT, "agendamento-notificacao-api/1.0")
                .build();
    }

    @Override
    public CanalNotificacaoEnum canal() {
        return CanalNotificacaoEnum.EMAIL;
    }

    @Override
    public String enviar(EntregaNotificacao entrega) {
        ResendResponse response = restClient.post()
                .uri("/emails")
                .header("Idempotency-Key", chaveIdempotencia(entrega))
                .body(new ResendRequest(
                        properties.from(),
                        List.of(entrega.getDestinatario()),
                        properties.subject(),
                        entrega.getAgendamento().getMensagem()
                ))
                .retrieve()
                .body(ResendResponse.class);

        if (response == null || response.id() == null || response.id().isBlank()) {
            throw new IllegalStateException("Resend não retornou o identificador do e-mail");
        }
        return response.id();
    }

    private String chaveIdempotencia(EntregaNotificacao entrega) {
        return "agendamento-%d-entrega-%d".formatted(
                entrega.getAgendamento().getId(),
                entrega.getId()
        );
    }

    private record ResendRequest(String from, List<String> to, String subject, String text) {
    }

    private record ResendResponse(String id) {
    }
}
