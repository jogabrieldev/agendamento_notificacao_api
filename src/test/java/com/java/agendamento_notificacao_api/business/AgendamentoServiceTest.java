package com.java.agendamento_notificacao_api.business;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.java.agendamento_notificacao_api.business.mapper.AgendamentoMapper;
import com.java.agendamento_notificacao_api.controller.dto.AgendamentoRecord;
import com.java.agendamento_notificacao_api.controller.out.AgendamentoRecordOut;
import com.java.agendamento_notificacao_api.infrastructure.entities.Agendamento;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusNotificacaoEnum;
import com.java.agendamento_notificacao_api.infrastructure.repositories.AgendamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgendamentoServiceTest {

    @InjectMocks
    private AgendamentoService agendamentoService;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private AgendamentoMapper agendamentoMapper;

    private AgendamentoRecord agendamentoRecord;
    private AgendamentoRecordOut agendamentoRecordOut;
    private Agendamento agendamentoEntity;

    @BeforeEach
    void setUp(){

        agendamentoEntity = new Agendamento(1L, "email@email.com", "55887996578",
                LocalDateTime.of(2025, 1, 2, 11, 1, 1),
                LocalDateTime.now(),null,
                "Favor retornar a loja com urgência",
                StatusNotificacaoEnum.AGENDADO);

        agendamentoRecord = new AgendamentoRecord("email@email.com", "5562999526384",
                "Por favor retorna a loja com urgência",
                LocalDateTime.of(2025, 1, 2, 11, 1, 1));

        agendamentoRecordOut = new AgendamentoRecordOut(1L,"mail@mail.com", "5562999526384",
                "Por favor retorna a loja com urgência",
                LocalDateTime.of(2025, 1, 2, 11, 1, 1),
                StatusNotificacaoEnum.AGENDADO);
    }

    @Test
    void deveGravarAgendamentoComSucesso(){
         when(agendamentoMapper.paraEntity(agendamentoRecord)).thenReturn(agendamentoEntity);
         when(agendamentoRepository.save(agendamentoEntity)).thenReturn(agendamentoEntity);
         when(agendamentoMapper.paraOut(agendamentoEntity)).thenReturn(agendamentoRecordOut);

         AgendamentoRecordOut out = agendamentoService.gravarAgendamento(agendamentoRecord);

         verify(agendamentoMapper , times(1)).paraEntity(agendamentoRecord);
         verify(agendamentoRepository , times(1)).save(agendamentoEntity);
         verify(agendamentoMapper, times(1)).paraOut(agendamentoEntity);
         assertThat(out).usingRecursiveComparison().isEqualTo(agendamentoRecordOut);
    }
}
