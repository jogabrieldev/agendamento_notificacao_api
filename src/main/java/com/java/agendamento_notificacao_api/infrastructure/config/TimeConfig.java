package com.java.agendamento_notificacao_api.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfig {

    public static final ZoneId BRAZIL_ZONE = ZoneId.of("America/Sao_Paulo");

    @Bean
    Clock applicationClock() {
        return Clock.systemUTC();
    }
}
