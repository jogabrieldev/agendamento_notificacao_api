package com.java.agendamento_notificacao_api.business.mapper;

import com.java.agendamento_notificacao_api.controller.dto.AgendamentoRecord;
import com.java.agendamento_notificacao_api.controller.out.AgendamentoRecordOut;
import com.java.agendamento_notificacao_api.infrastructure.entities.Agendamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface AgendamentoMapper {

    Agendamento paraEntity(AgendamentoRecord agendamento);

    @Mapping(target = "statusNotificacao", source = "statusNotificacao") // 🔹 aqui está o segredo
    AgendamentoRecordOut paraOut(Agendamento agendamento);
}
