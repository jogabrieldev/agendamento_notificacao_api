package com.java.agendamento_notificacao_api.infrastructure.repositories;

import com.java.agendamento_notificacao_api.infrastructure.entities.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long > {
}
