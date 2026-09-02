package com.java.agendamento_notificacao_api.business.provider.real;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "notification.provider.mode", havingValue = "production")
@EnableConfigurationProperties(RealProviderProperties.class)
public class RealProviderConfig {
}
