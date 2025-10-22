package com.java.agendamento_notificacao_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.java.agendamento_notificacao_api.business.AgendamentoService;
import com.java.agendamento_notificacao_api.controller.dto.AgendamentoRecord;
import com.java.agendamento_notificacao_api.controller.out.AgendamentoRecordOut;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusNotificacaoEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.MediaType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AgendamentoControllerTest {

    @Mock
    AgendamentoService agendamentoService;

    @InjectMocks
    AgendamentoController agendamentoController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    private AgendamentoRecord agendamentoRecord;
    private AgendamentoRecordOut agendamentoRecordOut;

    @BeforeEach
    void setUp(){

        mockMvc = MockMvcBuilders.standaloneSetup(agendamentoController).build();

        objectMapper.registerModule(new JavaTimeModule());
        agendamentoRecord = new AgendamentoRecord("email@email.com", "5562999526384",
                "Por favor retorna a loja com urgência",
                LocalDateTime.of(2025, 1, 2, 11, 1, 1));

        agendamentoRecordOut = new AgendamentoRecordOut(1L,"mail@mail.com", "5562999526384",
                "Por favor retorna a loja com urgência",
                LocalDateTime.of(2025, 1, 2, 11, 1, 1),
                StatusNotificacaoEnum.AGENDADO);
    }

    @Test
    void deveCriarAgendamentoComSucesso() throws Exception {
         when(agendamentoService.gravarAgendamento(agendamentoRecord)).thenReturn(agendamentoRecordOut);
         mockMvc.perform(post("/agendamento")
                 .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                 .content(objectMapper.writeValueAsBytes(agendamentoRecord)))
                 .andExpect(status().isOk())
                 .andExpect(jsonPath( "$.id").value(1L))
                 .andExpect(jsonPath("$.emailDestinatario").value("mail@mail.com"))
                 .andExpect(jsonPath("$.telefoneDestinatario").value(agendamentoRecordOut.telefoneDestinatario()))
                 .andExpect(jsonPath("$.mensagem").value(agendamentoRecordOut.mensagem()))
                 .andExpect(jsonPath("$.dataHoraEnvio").value("02-01-2025 11:01:01"))
                 .andExpect(jsonPath("$.statusNotificacao").value(agendamentoRecordOut.statusNotificacao().name()));


        verify(agendamentoService, times(1)).gravarAgendamento(agendamentoRecord);
    }

}
