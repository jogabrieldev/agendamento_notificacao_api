package com.java.agendamento_notificacao_api.infrastructure.repositories;

import com.java.agendamento_notificacao_api.infrastructure.entities.OutboxEvent;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusOutboxEnum;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select evento
            from OutboxEvent evento
            join fetch evento.entrega
            where evento.status = :status
              and evento.disponivelEm <= :limite
            order by evento.criadoEm asc
            """)
    List<OutboxEvent> buscarPendentesParaPublicacao(
            @Param("status") StatusOutboxEnum status,
            @Param("limite") Instant limite,
            Pageable pageable
    );
}
