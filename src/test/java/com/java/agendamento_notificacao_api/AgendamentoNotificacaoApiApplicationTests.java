package com.java.agendamento_notificacao_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.rabbitmq.listener.simple.auto-startup=false",
		"notification.outbox.fixed-delay-ms=60000"
})
class AgendamentoNotificacaoApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
