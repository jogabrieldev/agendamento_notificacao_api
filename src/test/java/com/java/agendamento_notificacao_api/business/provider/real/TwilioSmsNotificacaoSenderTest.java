package com.java.agendamento_notificacao_api.business.provider.real;

import com.java.agendamento_notificacao_api.infrastructure.entities.Agendamento;
import com.java.agendamento_notificacao_api.infrastructure.entities.EntregaNotificacao;
import com.java.agendamento_notificacao_api.infrastructure.enums.CanalNotificacaoEnum;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TwilioSmsNotificacaoSenderTest {

    @Test
    void deveEnviarFormularioERetornarSidDaTwilio() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RealProviderProperties properties = new RealProviderProperties(
                new RealProviderProperties.Resend("re_test", "notificacoes@example.com", "Aviso"),
                new RealProviderProperties.Twilio("AC123", "token-secreto", "+15005550006")
        );
        TwilioSmsNotificacaoSender sender = new TwilioSmsNotificacaoSender(builder, properties);

        server.expect(requestTo("https://api.twilio.com/2010-04-01/Accounts/AC123/Messages.json"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Basic QUMxMjM6dG9rZW4tc2VjcmV0bw=="))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string("From=%2B15005550006&To=%2B5562999999999&Body=Consulta+confirmada"))
                .andRespond(withSuccess("{\"sid\":\"SM123\",\"status\":\"queued\"}", MediaType.APPLICATION_JSON));

        assertThat(sender.enviar(entrega("+5562999999999"))).isEqualTo("SM123");
        server.verify();
    }

    private EntregaNotificacao entrega(String destinatario) {
        Agendamento agendamento = Agendamento.builder().id(10L).mensagem("Consulta confirmada").build();
        return EntregaNotificacao.builder()
                .id(20L)
                .agendamento(agendamento)
                .canal(CanalNotificacaoEnum.SMS)
                .destinatario(destinatario)
                .build();
    }
}
