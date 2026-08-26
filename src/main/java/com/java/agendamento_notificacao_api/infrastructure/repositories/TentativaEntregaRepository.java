package com.java.agendamento_notificacao_api.infrastructure.repositories;

import com.java.agendamento_notificacao_api.infrastructure.entities.TentativaEntrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TentativaEntregaRepository extends JpaRepository<TentativaEntrega, Long> {
    List<TentativaEntrega> findByEntregaIdOrderByNumeroTentativaAsc(Long entregaId);
}
