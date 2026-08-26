package com.java.agendamento_notificacao_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgendamentoNotificacaoApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgendamentoNotificacaoApiApplication.class, args);
	}

}
