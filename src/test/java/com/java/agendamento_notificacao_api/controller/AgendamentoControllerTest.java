package com.java.agendamento_notificacao_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.java.agendamento_notificacao_api.business.AgendamentoService;
import com.java.agendamento_notificacao_api.controller.dto.AgendamentoRequest;
import com.java.agendamento_notificacao_api.controller.out.AgendamentoResponse;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusNotificacaoEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoControllerTest {

    @Mock
    private AgendamentoService agendamentoService;

    @InjectMocks
    private AgendamentoController agendamentoController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Clock clock;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        clock = Clock.fixed(Instant.parse("2026-08-26T18:00:00Z"), ZoneOffset.UTC);
        mockMvc = MockMvcBuilders.standaloneSetup(agendamentoController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler(clock))
                .build();
    }

    @Test
    void deveCriarAgendamentoComSucesso() throws Exception {
        OffsetDateTime envio = OffsetDateTime.of(2027, 1, 2, 11, 1, 1, 0, ZoneOffset.ofHours(-3));
        AgendamentoRequest request = new AgendamentoRequest(
                "email@email.com",
                "+5562999526384",
                "Mensagem de teste",
                envio
        );
        AgendamentoResponse response = new AgendamentoResponse(
                1L,
                request.mensagem(),
                StatusNotificacaoEnum.AGENDADO,
                envio,
                clock.instant().atOffset(ZoneOffset.ofHours(-3)),
                null,
                List.of()
        );
        when(agendamentoService.gravarAgendamento(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/agendamentos/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("AGENDADO"));

        verify(agendamentoService).gravarAgendamento(request);
    }

    @Test
    void deveRejeitarAgendamentoSemDestinatario() throws Exception {
        AgendamentoRequest request = new AgendamentoRequest(
                null,
                null,
                "Mensagem de teste",
                OffsetDateTime.of(2027, 1, 2, 11, 1, 1, 0, ZoneOffset.ofHours(-3))
        );

        mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(agendamentoService);
    }

    @Test
    void deveRejeitarEmailSemDominioCompleto() throws Exception {
        AgendamentoRequest request = new AgendamentoRequest(
                "cliente@localhost",
                null,
                "Mensagem de teste",
                OffsetDateTime.of(2027, 1, 2, 11, 1, 1, 0, ZoneOffset.ofHours(-3))
        );

        mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.emailDestinatario").exists());

        verifyNoInteractions(agendamentoService);
    }

    @Test
    void deveRejeitarTelefoneForaDoPadraoE164() throws Exception {
        AgendamentoRequest request = new AgendamentoRequest(
                null,
                "(62) 99999-9999",
                "Mensagem de teste",
                OffsetDateTime.of(2027, 1, 2, 11, 1, 1, 0, ZoneOffset.ofHours(-3))
        );

        mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.telefoneDestinatario").exists());

        verifyNoInteractions(agendamentoService);
    }
}
