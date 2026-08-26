package com.java.agendamento_notificacao_api.infrastructure.entities;

import com.java.agendamento_notificacao_api.infrastructure.enums.CanalNotificacaoEnum;
import com.java.agendamento_notificacao_api.infrastructure.enums.StatusEntregaEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Table(
        name = "entrega_notificacao",
        indexes = {
                @Index(name = "idx_entrega_status_proxima_tentativa", columnList = "status, proxima_tentativa_em"),
                @Index(name = "idx_entrega_agendamento", columnList = "agendamento_id")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_entrega_agendamento_canal_destinatario",
                columnNames = {"agendamento_id", "canal", "destinatario"}
        )
)
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntregaNotificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalNotificacaoEnum canal;

    @Column(nullable = false, length = 320)
    private String destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusEntregaEnum status;

    @Column(name = "quantidade_tentativas", nullable = false)
    private Integer quantidadeTentativas;

    @Column(name = "proxima_tentativa_em", columnDefinition = "timestamp with time zone")
    private Instant proximaTentativaEm;

    @Column(name = "processada_em", columnDefinition = "timestamp with time zone")
    private Instant processadaEm;

    @Column(name = "enviada_em", columnDefinition = "timestamp with time zone")
    private Instant enviadaEm;

    @Column(name = "ultimo_erro", length = 1000)
    private String ultimoErro;

    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Version
    private Long version;

    @OneToMany(mappedBy = "entrega", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TentativaEntrega> tentativas = new ArrayList<>();

    @OneToMany(mappedBy = "entrega", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OutboxEvent> eventos = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        if (status == null) {
            status = StatusEntregaEnum.PENDENTE;
        }
        if (quantidadeTentativas == null) {
            quantidadeTentativas = 0;
        }
    }

    public void adicionarTentativa(TentativaEntrega tentativa) {
        tentativas.add(tentativa);
        tentativa.setEntrega(this);
    }

    public void adicionarEvento(OutboxEvent evento) {
        eventos.add(evento);
        evento.setEntrega(this);
    }
}
