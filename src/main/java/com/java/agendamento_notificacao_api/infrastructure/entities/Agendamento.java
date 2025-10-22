package com.java.agendamento_notificacao_api.infrastructure.entities;

import com.java.agendamento_notificacao_api.infrastructure.enums.StatusNotificacaoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "agendamento")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String emailDestinatario;
    private String telefoneDestinatario;
    private LocalDateTime dataHoraEnvio;
    private LocalDateTime dataHoraAgendamento;
    private LocalDateTime dataHoraNotificacao;
    private String mensagem;

    @Enumerated(EnumType.STRING)
    private StatusNotificacaoEnum statusNotificacao;

    @PrePersist
    private void prePersist(){
        if (dataHoraAgendamento == null)
            dataHoraAgendamento = LocalDateTime.now();
        if (statusNotificacao == null)
            statusNotificacao = StatusNotificacaoEnum.AGENDADO;
    }

}
