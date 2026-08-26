package com.java.agendamento_notificacao_api.business;

import com.java.agendamento_notificacao_api.infrastructure.entities.EntregaNotificacao;
import com.java.agendamento_notificacao_api.infrastructure.entities.TentativaEntrega;
import com.java.agendamento_notificacao_api.infrastructure.enums.ResultadoTentativaEnum;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusEntregaEnum;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusNotificacaoEnum;
import com.java.agendamento_notificacao_api.infrastructure.repositories.EntregaNotificacaoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FalhaEntregaService {

    private static final int LIMITE_TENTATIVAS = 3;

    private final EntregaNotificacaoRepository entregaRepository;
    private final Clock clock;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void registrar(Long entregaId, Exception exception) {
        EntregaNotificacao entrega = entregaRepository.findById(entregaId)
                .orElse(null);
        if (entrega == null || entrega.getStatus() == StatusEntregaEnum.ENVIADA) {
            return;
        }

        int numeroTentativa = entrega.getQuantidadeTentativas() + 1;
        Instant agora = clock.instant();
        boolean falhaDefinitiva = numeroTentativa >= LIMITE_TENTATIVAS;

        entrega.adicionarTentativa(TentativaEntrega.builder()
                .numeroTentativa(numeroTentativa)
                .iniciadaEm(agora)
                .finalizadaEm(agora)
                .resultado(falhaDefinitiva
                        ? ResultadoTentativaEnum.FALHA_DEFINITIVA
                        : ResultadoTentativaEnum.FALHA_TRANSITORIA)
                .mensagemErro(exception.getMessage())
                .build());
        entrega.setQuantidadeTentativas(numeroTentativa);
        entrega.setUltimoErro(exception.getMessage());
        entrega.setStatus(falhaDefinitiva
                ? StatusEntregaEnum.FALHA_DEFINITIVA
                : StatusEntregaEnum.AGUARDANDO_RETRY);
        if (falhaDefinitiva) {
            entrega.getAgendamento().setStatusNotificacao(StatusNotificacaoEnum.FALHA);
        }
        entrega.getAgendamento().setDataHoraModificacao(agora);
    }
}
