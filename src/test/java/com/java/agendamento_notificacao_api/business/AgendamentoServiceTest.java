package com.java.agendamento_notificacao_api.business;

import com.java.agendamento_notificacao_api.business.mapper.AgendamentoMapper;
import com.java.agendamento_notificacao_api.controller.dto.AgendamentoRequest;
import com.java.agendamento_notificacao_api.controller.out.AgendamentoResponse;
import com.java.agendamento_notificacao_api.infrastructure.entities.Agendamento;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusNotificacaoEnum;
import com.java.agendamento_notificacao_api.infrastructure.exception.DataAgendamentoInvalidaException;
import com.java.agendamento_notificacao_api.infrastructure.repositories.AgendamentoRepository;
import com.java.agendamento_notificacao_api.infrastructure.repositories.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @InjectMocks
    private AgendamentoService agendamentoService;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private AgendamentoMapper agendamentoMapper;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private Clock clock;

    private AgendamentoRequest request;
    private Agendamento entity;

    @BeforeEach
    void setUp() {
        OffsetDateTime envio = OffsetDateTime.of(2027, 1, 2, 11, 1, 1, 0, ZoneOffset.ofHours(-3));
        Instant agora = Instant.parse("2026-08-26T18:00:00Z");
        request = new AgendamentoRequest(
                "email@email.com",
                "+5562999526384",
                "Por favor, retorne à loja com urgência",
                envio
        );
        entity = new Agendamento(
                1L,
                request.emailDestinatario(),
                request.telefoneDestinatario(),
                envio.toInstant(),
                agora,
                null,
                request.mensagem(),
                StatusNotificacaoEnum.AGENDADO
        );
    }

    @Test
    void deveGravarAgendamentoComEntregasEOutbox() {
        when(clock.instant()).thenReturn(Instant.parse("2026-08-26T18:00:00Z"));
        when(agendamentoMapper.paraEntity(request)).thenReturn(entity);
        when(agendamentoRepository.saveAndFlush(entity)).thenReturn(entity);

        AgendamentoResponse response = agendamentoService.gravarAgendamento(request);

        verify(agendamentoRepository).saveAndFlush(entity);
        verify(outboxEventRepository).saveAll(anyList());
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.entregas()).hasSize(2);
        assertThat(response.status()).isEqualTo(StatusNotificacaoEnum.AGENDADO);
    }

    @Test
    void deveRejeitarOffsetDiferenteDoHorarioDeBrasilia() {
        AgendamentoRequest horarioUtc = new AgendamentoRequest(
                request.emailDestinatario(),
                request.telefoneDestinatario(),
                request.mensagem(),
                request.dataHoraEnvio().withOffsetSameInstant(ZoneOffset.UTC)
        );

        assertThatThrownBy(() -> agendamentoService.gravarAgendamento(horarioUtc))
                .isInstanceOf(DataAgendamentoInvalidaException.class)
                .hasMessageContaining("America/Sao_Paulo");
        verifyNoInteractions(agendamentoRepository, agendamentoMapper, outboxEventRepository);
    }

    @Test
    void deveRejeitarHorarioSemAntecedenciaMinima() {
        Instant agora = Instant.parse("2026-08-26T18:00:00Z");
        when(clock.instant()).thenReturn(agora);
        ReflectionTestUtils.setField(agendamentoService, "minimumLeadSeconds", 5L);
        AgendamentoRequest muitoProximo = new AgendamentoRequest(
                request.emailDestinatario(),
                request.telefoneDestinatario(),
                request.mensagem(),
                agora.plusSeconds(4).atOffset(ZoneOffset.ofHours(-3))
        );

        assertThatThrownBy(() -> agendamentoService.gravarAgendamento(muitoProximo))
                .isInstanceOf(DataAgendamentoInvalidaException.class)
                .hasMessageContaining("5 segundos");
        verifyNoInteractions(agendamentoRepository, agendamentoMapper, outboxEventRepository);
    }
}
