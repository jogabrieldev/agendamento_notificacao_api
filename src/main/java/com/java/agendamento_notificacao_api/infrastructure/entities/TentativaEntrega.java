package com.java.agendamento_notificacao_api.infrastructure.entities;

import com.java.agendamento_notificacao_api.infrastructure.enums.ResultadoTentativaEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Table(
        name = "tentativa_entrega",
        indexes = @Index(name = "idx_tentativa_entrega", columnList = "entrega_id")
)
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TentativaEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entrega_id", nullable = false)
    private EntregaNotificacao entrega;

    @Column(name = "numero_tentativa", nullable = false)
    private Integer numeroTentativa;

    @Column(name = "iniciada_em", nullable = false, columnDefinition = "timestamp with time zone")
    private Instant iniciadaEm;

    @Column(name = "finalizada_em", columnDefinition = "timestamp with time zone")
    private Instant finalizadaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResultadoTentativaEnum resultado;

    @Column(name = "codigo_provedor", length = 100)
    private String codigoProvedor;

    @Column(name = "mensagem_erro", length = 1000)
    private String mensagemErro;

    @PrePersist
    private void prePersist() {
        if (iniciadaEm == null) {
            iniciadaEm = Instant.now();
        }
        if (resultado == null) {
            resultado = ResultadoTentativaEnum.INICIADA;
        }
    }
}
