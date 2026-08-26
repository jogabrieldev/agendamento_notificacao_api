package com.java.agendamento_notificacao_api.infrastructure.repositories;

import com.java.agendamento_notificacao_api.infrastructure.entities.OutboxEvent;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusOutboxEnum;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByStatusAndDisponivelEmLessThanEqualOrderByCriadoEmAsc(
            StatusOutboxEnum status,
            LocalDateTime limite,
            Pageable pageable
    );
}
