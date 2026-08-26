package com.java.agendamento_notificacao_api.infrastructure.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI notificacaoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Agendamento de Notificações")
                        .version("v1")
                        .description("""
                                API para agendar, consultar e cancelar notificações por e-mail e SMS.
                                Os agendamentos são persistidos no PostgreSQL, publicados de forma confiável
                                através de outbox e processados assincronamente pelo RabbitMQ.

                                Neste ambiente, os provedores de e-mail e SMS operam em modo de simulação.
                                """)
                        .contact(new Contact().name("Equipe da API"))
                        .license(new License().name("Uso educacional")))
                .tags(List.of(new Tag()
                        .name("Agendamentos")
                        .description("Ciclo de vida das notificações agendadas")))
                .externalDocs(new ExternalDocumentation()
                        .description("Especificação OpenAPI gerada pela aplicação")
                        .url("/v3/api-docs"));
    }
}
