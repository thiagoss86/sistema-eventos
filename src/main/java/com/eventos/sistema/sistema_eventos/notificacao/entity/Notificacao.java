package com.eventos.sistema.sistema_eventos.notificacao.entity;

import com.eventos.sistema.sistema_eventos.participante.entity.Participante;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "participante_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_notificacao_participante")
    )
    private Participante participante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoNotificacao tipoNotificacao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(nullable = false)
    private LocalDateTime data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StatusNotificacao status = StatusNotificacao.PENDENTE;
}
