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

class ResendEmailNotificacaoSenderTest {

    @Test
    void deveEnviarEmailComIdempotenciaERetornarIdDoResend() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RealProviderProperties properties = new RealProviderProperties(
                new RealProviderProperties.Resend("re_test", "Sistema <notificacoes@example.com>", "Aviso"),
                new RealProviderProperties.Twilio("AC_test", "token", "+15005550006")
        );
        ResendEmailNotificacaoSender sender = new ResendEmailNotificacaoSender(builder, properties);

        server.expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer re_test"))
                .andExpect(header("Idempotency-Key", "agendamento-10-entrega-20"))
                .andExpect(content().json("""
                        {
                          "from": "Sistema <notificacoes@example.com>",
                          "to": ["cliente@example.com"],
                          "subject": "Aviso",
                          "text": "Consulta confirmada"
                        }
                        """))
                .andRespond(withSuccess("{\"id\":\"email_123\"}", MediaType.APPLICATION_JSON));

        assertThat(sender.enviar(entrega(CanalNotificacaoEnum.EMAIL, "cliente@example.com")))
                .isEqualTo("email_123");
        server.verify();
    }

    private EntregaNotificacao entrega(CanalNotificacaoEnum canal, String destinatario) {
        Agendamento agendamento = Agendamento.builder().id(10L).mensagem("Consulta confirmada").build();
        EntregaNotificacao entrega = EntregaNotificacao.builder()
                .id(20L)
                .agendamento(agendamento)
                .canal(canal)
                .destinatario(destinatario)
                .build();
        return entrega;
    }
}
