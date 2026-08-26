package com.java.agendamento_notificacao_api.infrastructure.entities;

import com.java.agendamento_notificacao_api.infrastructure.enums.StatusNotificacaoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Table(name = "agendamento")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agendamento {

    public Agendamento(Long id, String emailDestinatario, String telefoneDestinatario,
                       Instant dataHoraEnvio, Instant dataHoraAgendamento,
                       Instant dataHoraModificacao, String mensagem,
                       StatusNotificacaoEnum statusNotificacao) {
        this.id = id;
        this.emailDestinatario = emailDestinatario;
        this.telefoneDestinatario = telefoneDestinatario;
        this.dataHoraEnvio = dataHoraEnvio;
        this.dataHoraAgendamento = dataHoraAgendamento;
        this.dataHoraModificacao = dataHoraModificacao;
        this.mensagem = mensagem;
        this.statusNotificacao = statusNotificacao;
        this.entregas = new ArrayList<>();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String emailDestinatario;
    private String telefoneDestinatario;
    @Column(columnDefinition = "timestamp with time zone")
    private Instant dataHoraEnvio;
    @Column(columnDefinition = "timestamp with time zone")
    private Instant dataHoraAgendamento;
    @Column(columnDefinition = "timestamp with time zone")
    private Instant dataHoraModificacao;
    private String mensagem;

    @Enumerated(EnumType.STRING)
    private StatusNotificacaoEnum statusNotificacao;

    @OneToMany(mappedBy = "agendamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EntregaNotificacao> entregas = new ArrayList<>();

    public void adicionarEntrega(EntregaNotificacao entrega) {
        entregas.add(entrega);
        entrega.setAgendamento(this);
    }

    public void removerEntrega(EntregaNotificacao entrega) {
        entregas.remove(entrega);
        entrega.setAgendamento(null);
    }

    @PrePersist
    private void prePersist(){
        if (dataHoraAgendamento == null)
            dataHoraAgendamento = Instant.now();
        if (statusNotificacao == null)
            statusNotificacao = StatusNotificacaoEnum.AGENDADO;
    }

}
