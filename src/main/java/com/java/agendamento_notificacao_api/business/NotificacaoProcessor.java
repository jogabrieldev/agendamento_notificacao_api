package com.java.agendamento_notificacao_api.business;

import com.java.agendamento_notificacao_api.business.message.NotificacaoMessage;
import com.java.agendamento_notificacao_api.business.provider.NotificacaoSender;
import com.java.agendamento_notificacao_api.infrastructure.entities.Agendamento;
import com.java.agendamento_notificacao_api.infrastructure.entities.EntregaNotificacao;
import com.java.agendamento_notificacao_api.infrastructure.entities.TentativaEntrega;
import com.java.agendamento_notificacao_api.infrastructure.enums.CanalNotificacaoEnum;
import com.java.agendamento_notificacao_api.infrastructure.enums.ResultadoTentativaEnum;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusEntregaEnum;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusNotificacaoEnum;
import com.java.agendamento_notificacao_api.infrastructure.exception.NotFoudException;
import com.java.agendamento_notificacao_api.infrastructure.repositories.EntregaNotificacaoRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NotificacaoProcessor {

    private final EntregaNotificacaoRepository entregaRepository;
    private final Map<CanalNotificacaoEnum, NotificacaoSender> senders;
    private final Clock clock;

    public NotificacaoProcessor(
            EntregaNotificacaoRepository entregaRepository,
            List<NotificacaoSender> senders,
            Clock clock
    ) {
        this.entregaRepository = entregaRepository;
        this.senders = new EnumMap<>(CanalNotificacaoEnum.class);
        this.clock = clock;
        senders.forEach(sender -> this.senders.put(sender.canal(), sender));
    }

    @Transactional
    public void processar(NotificacaoMessage message, CanalNotificacaoEnum canalEsperado) {
        EntregaNotificacao entrega = entregaRepository.findById(message.entregaId())
                .orElseThrow(() -> new NotFoudException("Entrega não encontrada"));

        validarMensagem(message, entrega, canalEsperado);
        if (entrega.getStatus() == StatusEntregaEnum.ENVIADA) {
            log.info("Entrega já processada: entregaId={}", entrega.getId());
            return;
        }
        if (entrega.getStatus() == StatusEntregaEnum.CANCELADA) {
            log.info("Entrega cancelada ignorada: entregaId={}", entrega.getId());
            return;
        }

        NotificacaoSender sender = senders.get(canalEsperado);
        if (sender == null) {
            throw new IllegalStateException("Provedor não configurado para " + canalEsperado);
        }

        Instant inicio = clock.instant();
        entrega.setStatus(StatusEntregaEnum.PROCESSANDO);
        entrega.setProcessadaEm(inicio);

        TentativaEntrega tentativa = TentativaEntrega.builder()
                .numeroTentativa(entrega.getQuantidadeTentativas() + 1)
                .iniciadaEm(inicio)
                .resultado(ResultadoTentativaEnum.INICIADA)
                .build();
        entrega.adicionarTentativa(tentativa);

        String providerMessageId = sender.enviar(entrega);
        Instant conclusao = clock.instant();
        tentativa.setResultado(ResultadoTentativaEnum.SUCESSO);
        tentativa.setFinalizadaEm(conclusao);
        tentativa.setCodigoProvedor(providerMessageId);
        entrega.setQuantidadeTentativas(tentativa.getNumeroTentativa());
        entrega.setProviderMessageId(providerMessageId);
        entrega.setEnviadaEm(conclusao);
        entrega.setUltimoErro(null);
        entrega.setStatus(StatusEntregaEnum.ENVIADA);
        atualizarStatusAgendamento(entrega.getAgendamento());
    }

    private void validarMensagem(
            NotificacaoMessage message,
            EntregaNotificacao entrega,
            CanalNotificacaoEnum canalEsperado
    ) {
        if (!entrega.getAgendamento().getId().equals(message.agendamentoId())) {
            throw new IllegalArgumentException("Agendamento da mensagem não corresponde à entrega");
        }
        if (entrega.getCanal() != message.canal() || entrega.getCanal() != canalEsperado) {
            throw new IllegalArgumentException("Canal da mensagem não corresponde à fila");
        }
    }

    private void atualizarStatusAgendamento(Agendamento agendamento) {
        boolean todasEnviadas = agendamento.getEntregas().stream()
                .allMatch(entrega -> entrega.getStatus() == StatusEntregaEnum.ENVIADA);
        boolean algumaEnviada = agendamento.getEntregas().stream()
                .anyMatch(entrega -> entrega.getStatus() == StatusEntregaEnum.ENVIADA);

        if (todasEnviadas) {
            agendamento.setStatusNotificacao(StatusNotificacaoEnum.ENVIADO);
        } else if (algumaEnviada) {
            agendamento.setStatusNotificacao(StatusNotificacaoEnum.PARCIALMENTE_ENVIADO);
        } else {
            agendamento.setStatusNotificacao(StatusNotificacaoEnum.PROCESSANDO);
        }
        agendamento.setDataHoraModificacao(clock.instant());
    }
}
