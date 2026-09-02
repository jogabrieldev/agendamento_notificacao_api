package com.java.agendamento_notificacao_api.business;

import com.java.agendamento_notificacao_api.business.mapper.AgendamentoMapper;
import com.java.agendamento_notificacao_api.controller.dto.AgendamentoRequest;
import com.java.agendamento_notificacao_api.controller.out.AgendamentoResponse;
import com.java.agendamento_notificacao_api.controller.out.AgendamentoResumoResponse;
import com.java.agendamento_notificacao_api.controller.out.EntregaNotificacaoResponse;
import com.java.agendamento_notificacao_api.controller.out.PaginaResponse;
import com.java.agendamento_notificacao_api.infrastructure.config.RabbitMqConstants;
import com.java.agendamento_notificacao_api.infrastructure.config.TimeConfig;
import com.java.agendamento_notificacao_api.infrastructure.entities.Agendamento;
import com.java.agendamento_notificacao_api.infrastructure.entities.EntregaNotificacao;
import com.java.agendamento_notificacao_api.infrastructure.entities.OutboxEvent;
import com.java.agendamento_notificacao_api.infrastructure.enums.CanalNotificacaoEnum;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusEntregaEnum;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusNotificacaoEnum;
import com.java.agendamento_notificacao_api.infrastructure.exception.NotFoudException;
import com.java.agendamento_notificacao_api.infrastructure.exception.RegraNegocioException;
import com.java.agendamento_notificacao_api.infrastructure.exception.DataAgendamentoInvalidaException;
import com.java.agendamento_notificacao_api.infrastructure.repositories.AgendamentoRepository;
import com.java.agendamento_notificacao_api.infrastructure.repositories.OutboxEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AgendamentoService {
    private final AgendamentoRepository repository;
    private final AgendamentoMapper agendamentoMapper;
    private final OutboxEventRepository outboxEventRepository;
    private final Clock clock;

    @Value("${notification.scheduling.minimum-lead-seconds:5}")
    private long minimumLeadSeconds;

    @Transactional
    public AgendamentoResponse gravarAgendamento(AgendamentoRequest request) {
        validarHorarioEnvio(request.dataHoraEnvio());
        Agendamento entity = agendamentoMapper.paraEntity(request);
        entity.setDataHoraAgendamento(clock.instant());
        adicionarEntrega(entity, CanalNotificacaoEnum.EMAIL, request.emailDestinatario());
        adicionarEntrega(entity, CanalNotificacaoEnum.SMS, request.telefoneDestinatario());

        Agendamento salvo = repository.saveAndFlush(entity);
        List<OutboxEvent> eventos = salvo.getEntregas().stream()
                .map(entrega -> criarEventoOutbox(salvo, entrega))
                .toList();
        outboxEventRepository.saveAll(eventos);
        return paraResponse(salvo);
    }

    @Transactional
    public AgendamentoResponse buscarAgendamentoPorId(Long id) {
        return paraResponse(buscarPorId(id));
    }

    @Transactional
    public PaginaResponse<AgendamentoResumoResponse> listarAgendamentos(int pagina, int tamanho) {
        PageRequest pageRequest = PageRequest.of(
                pagina,
                tamanho,
                Sort.by(Sort.Direction.DESC, "dataHoraAgendamento")
        );
        Page<AgendamentoResumoResponse> resultado = repository.findAll(pageRequest).map(this::paraResumo);
        return PaginaResponse.de(resultado);
    }

    @Transactional
    public void cancelarAgendamento(Long id) {
        Agendamento agendamento = buscarPorId(id);
        if (agendamento.getStatusNotificacao() == StatusNotificacaoEnum.ENVIADO) {
            throw new RegraNegocioException("Uma notificação enviada não pode ser cancelada");
        }
        if (agendamento.getStatusNotificacao() == StatusNotificacaoEnum.CANCELADO) {
            return;
        }

        agendamento.setStatusNotificacao(StatusNotificacaoEnum.CANCELADO);
        agendamento.setDataHoraModificacao(clock.instant());
        agendamento.getEntregas().stream()
                .filter(entrega -> entrega.getStatus() != StatusEntregaEnum.ENVIADA)
                .forEach(entrega -> entrega.setStatus(StatusEntregaEnum.CANCELADA));
    }

    private Agendamento buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoudException("Agendamento não encontrado"));
    }

    private void adicionarEntrega(
            Agendamento agendamento,
            CanalNotificacaoEnum canal,
            String destinatario
    ) {
        if (destinatario != null && !destinatario.isBlank()) {
            agendamento.adicionarEntrega(EntregaNotificacao.builder()
                    .canal(canal)
                    .destinatario(destinatario)
                    .build());
        }
    }

    private OutboxEvent criarEventoOutbox(Agendamento agendamento, EntregaNotificacao entrega) {
        String routingKey = entrega.getCanal() == CanalNotificacaoEnum.EMAIL
                ? RabbitMqConstants.EMAIL_ROUTING_KEY
                : RabbitMqConstants.SMS_ROUTING_KEY;
        String payload = "{\"agendamentoId\":%d,\"entregaId\":%d,\"canal\":\"%s\"}"
                .formatted(agendamento.getId(), entrega.getId(), entrega.getCanal());

        OutboxEvent evento = OutboxEvent.builder()
                .tipoEvento("NOTIFICACAO_PRONTA_PARA_ENVIO")
                .routingKey(routingKey)
                .payload(payload)
                .criadoEm(clock.instant())
                .disponivelEm(agendamento.getDataHoraEnvio())
                .build();
        entrega.adicionarEvento(evento);
        return evento;
    }

    private AgendamentoResponse paraResponse(Agendamento agendamento) {
        List<EntregaNotificacaoResponse> entregas = agendamento.getEntregas().stream()
                .map(this::paraEntregaResponse)
                .toList();
        return new AgendamentoResponse(
                agendamento.getId(),
                agendamento.getMensagem(),
                agendamento.getStatusNotificacao(),
                paraHorarioBrasil(agendamento.getDataHoraEnvio()),
                paraHorarioBrasil(agendamento.getDataHoraAgendamento()),
                paraHorarioBrasil(agendamento.getDataHoraModificacao()),
                entregas
        );
    }

    private EntregaNotificacaoResponse paraEntregaResponse(EntregaNotificacao entrega) {
        return new EntregaNotificacaoResponse(
                entrega.getId(),
                entrega.getCanal(),
                entrega.getDestinatario(),
                entrega.getStatus(),
                entrega.getQuantidadeTentativas(),
                entrega.getProviderMessageId(),
                entrega.getUltimoErro(),
                paraHorarioBrasil(entrega.getProcessadaEm()),
                paraHorarioBrasil(entrega.getEnviadaEm())
        );
    }

    private AgendamentoResumoResponse paraResumo(Agendamento agendamento) {
        return new AgendamentoResumoResponse(
                agendamento.getId(),
                agendamento.getMensagem(),
                agendamento.getStatusNotificacao(),
                paraHorarioBrasil(agendamento.getDataHoraEnvio()),
                agendamento.getEntregas().size()
        );
    }

    private void validarHorarioEnvio(OffsetDateTime dataHoraEnvio) {
        if (dataHoraEnvio == null) {
            return;
        }
        Instant instanteEnvio = dataHoraEnvio.toInstant();
        if (!TimeConfig.BRAZIL_ZONE.getRules().getOffset(instanteEnvio).equals(dataHoraEnvio.getOffset())) {
            throw new DataAgendamentoInvalidaException(
                    "Data e hora de envio devem utilizar o offset vigente de America/Sao_Paulo"
            );
        }
        if (instanteEnvio.isBefore(clock.instant().plusSeconds(minimumLeadSeconds))) {
            throw new DataAgendamentoInvalidaException(
                    "Data e hora de envio devem respeitar a antecedência mínima de "
                            + minimumLeadSeconds + " segundos"
            );
        }
    }

    private OffsetDateTime paraHorarioBrasil(Instant instante) {
        return instante == null ? null : instante.atZone(TimeConfig.BRAZIL_ZONE).toOffsetDateTime();
    }
}
