package com.java.agendamento_notificacao_api.infrastructure.repositories;

import com.java.agendamento_notificacao_api.infrastructure.entities.EntregaNotificacao;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusEntregaEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EntregaNotificacaoRepository extends JpaRepository<EntregaNotificacao, Long> {
    List<EntregaNotificacao> findByStatusAndProximaTentativaEmLessThanEqual(
            StatusEntregaEnum status,
            LocalDateTime limite
    );
}
