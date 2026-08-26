package com.java.agendamento_notificacao_api.infrastructure.entities;

import com.java.agendamento_notificacao_api.infrastructure.enums.StatusOutboxEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Table(
        name = "outbox_event",
        indexes = @Index(name = "idx_outbox_status_disponivel_em", columnList = "status, disponivel_em")
)
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entrega_id", nullable = false)
    private EntregaNotificacao entrega;

    @Column(name = "tipo_evento", nullable = false, length = 100)
    private String tipoEvento;

    @Column(name = "routing_key", nullable = false, length = 100)
    private String routingKey;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusOutboxEnum status;

    @Column(name = "criado_em", nullable = false, columnDefinition = "timestamp with time zone")
    private Instant criadoEm;

    @Column(name = "disponivel_em", nullable = false, columnDefinition = "timestamp with time zone")
    private Instant disponivelEm;

    @Column(name = "publicado_em", columnDefinition = "timestamp with time zone")
    private Instant publicadoEm;

    @Column(name = "quantidade_tentativas", nullable = false)
    private Integer quantidadeTentativas;

    @Column(name = "ultimo_erro", length = 1000)
    private String ultimoErro;

    @PrePersist
    private void prePersist() {
        if (status == null) {
            status = StatusOutboxEnum.PENDENTE;
        }
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
        if (disponivelEm == null) {
            disponivelEm = criadoEm;
        }
        if (quantidadeTentativas == null) {
            quantidadeTentativas = 0;
        }
    }
}
