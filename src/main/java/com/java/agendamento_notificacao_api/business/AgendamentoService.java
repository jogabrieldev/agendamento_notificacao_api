package com.java.agendamento_notificacao_api.business;

import com.java.agendamento_notificacao_api.business.mapper.AgendamentoMapper;
import com.java.agendamento_notificacao_api.controller.dto.AgendamentoRecord;
import com.java.agendamento_notificacao_api.controller.out.AgendamentoRecordOut;
import com.java.agendamento_notificacao_api.infrastructure.config.RabbitMqConstants;
import com.java.agendamento_notificacao_api.infrastructure.entities.Agendamento;
import com.java.agendamento_notificacao_api.infrastructure.entities.EntregaNotificacao;
import com.java.agendamento_notificacao_api.infrastructure.entities.OutboxEvent;
import com.java.agendamento_notificacao_api.infrastructure.enums.CanalNotificacaoEnum;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusEntregaEnum;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusNotificacaoEnum;
import com.java.agendamento_notificacao_api.infrastructure.exception.NotFoudException;
import com.java.agendamento_notificacao_api.infrastructure.repositories.AgendamentoRepository;
import com.java.agendamento_notificacao_api.infrastructure.repositories.OutboxEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AgendamentoService {
    private final AgendamentoRepository repository;
    private final AgendamentoMapper agendamentoMapper;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public AgendamentoRecordOut gravarAgendamento(AgendamentoRecord agendamento) {
        Agendamento entity = agendamentoMapper.paraEntity(agendamento);
        adicionarEntrega(entity, CanalNotificacaoEnum.EMAIL, agendamento.emailDestinatario());
        adicionarEntrega(entity, CanalNotificacaoEnum.SMS, agendamento.telefoneDestinatario());

        Agendamento salvo = repository.saveAndFlush(entity);
        List<OutboxEvent> eventos = salvo.getEntregas().stream()
                .map(entrega -> criarEventoOutbox(salvo, entrega))
                .toList();
        outboxEventRepository.saveAll(eventos);

        return agendamentoMapper.paraOut(salvo);
    }

    public AgendamentoRecordOut buscarAgendamentoPorId(Long id) {
        return agendamentoMapper.paraOut(
                repository.findById(id)
                        .orElseThrow(() -> new NotFoudException("ID não encontrado")));
    }

    @Transactional
    public void cancelarAgendamento(Long id) {
        Agendamento agendamento = repository.findById(id)
                .orElseThrow(() -> new NotFoudException("ID não encontrado"));
        agendamento.setStatusNotificacao(StatusNotificacaoEnum.CANCELADO);
        agendamento.setDataHoraModificacao(LocalDateTime.now());
        agendamento.getEntregas().forEach(entrega ->
                entrega.setStatus(StatusEntregaEnum.CANCELADA));
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
                .disponivelEm(agendamento.getDataHoraEnvio())
                .build();
        entrega.adicionarEvento(evento);
        return evento;
    }
}
