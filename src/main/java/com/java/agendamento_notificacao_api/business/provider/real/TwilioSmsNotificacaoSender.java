package com.java.agendamento_notificacao_api.business.provider.real;

import com.java.agendamento_notificacao_api.business.provider.NotificacaoSender;
import com.java.agendamento_notificacao_api.infrastructure.entities.EntregaNotificacao;
import com.java.agendamento_notificacao_api.infrastructure.enums.CanalNotificacaoEnum;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "notification.provider.mode", havingValue = "production")
public class TwilioSmsNotificacaoSender implements NotificacaoSender {

    private final RestClient restClient;
    private final RealProviderProperties.Twilio properties;

    public TwilioSmsNotificacaoSender(RestClient.Builder builder, RealProviderProperties properties) {
        this.properties = properties.twilio();
        this.restClient = builder
                .baseUrl("https://api.twilio.com/2010-04-01")
                .defaultHeaders(headers -> headers.setBasicAuth(
                        this.properties.accountSid(),
                        this.properties.authToken()
                ))
                .build();
    }

    @Override
    public CanalNotificacaoEnum canal() {
        return CanalNotificacaoEnum.SMS;
    }

    @Override
    public String enviar(EntregaNotificacao entrega) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("From", properties.from());
        form.add("To", entrega.getDestinatario());
        form.add("Body", entrega.getAgendamento().getMensagem());

        TwilioResponse response = restClient.post()
                .uri("/Accounts/{accountSid}/Messages.json", properties.accountSid())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TwilioResponse.class);

        if (response == null || response.sid() == null || response.sid().isBlank()) {
            throw new IllegalStateException("Twilio não retornou o identificador do SMS");
        }
        return response.sid();
    }

    private record TwilioResponse(String sid, String status) {
    }
}
