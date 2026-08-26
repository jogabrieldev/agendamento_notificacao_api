package com.java.agendamento_notificacao_api.business.mapper;

import com.java.agendamento_notificacao_api.controller.dto.AgendamentoRecord;
import com.java.agendamento_notificacao_api.controller.dto.AgendamentoRequest;
import com.java.agendamento_notificacao_api.controller.out.AgendamentoRecordOut;
import com.java.agendamento_notificacao_api.infrastructure.config.TimeConfig;
import com.java.agendamento_notificacao_api.infrastructure.entities.Agendamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.OffsetDateTime;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, imports = Instant.class)
public interface AgendamentoMapper {

    Agendamento paraEntity(AgendamentoRecord agendamento);

    Agendamento paraEntity(AgendamentoRequest agendamento);

    @Mapping(target = "statusNotificacao", source = "statusNotificacao") //
    AgendamentoRecordOut paraOut(Agendamento agendamento);

    @Mapping(target = "dataHoraModificacao", expression = "java(Instant.now())")
    @Mapping(target = "statusNotificacao", expression = "java(StatusNotificacaoEnum.CANCELADO)")
    Agendamento paraEntityCancelamento(Agendamento agendamento);

    default Instant paraInstant(OffsetDateTime dataHora) {
        return dataHora == null ? null : dataHora.toInstant();
    }

    default OffsetDateTime paraOffsetDateTime(Instant instante) {
        return instante == null
                ? null
                : instante.atZone(TimeConfig.BRAZIL_ZONE).toOffsetDateTime();
    }
}
