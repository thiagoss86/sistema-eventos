package com.eventos.sistema.sistema_eventos.inscricao.entity;

import com.eventos.sistema.sistema_eventos.evento.entity.Evento;
import com.eventos.sistema.sistema_eventos.participante.entity.Participante;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inscricoes",
        uniqueConstraints =
        @UniqueConstraint(
                name = "uk_inscricao_evento_participante",
                columnNames = {"evento_id", "participante_id"}
        ))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Inscricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "evento_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_inscricao_participante")
    )
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "participante_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_inscricao_participante")
    )
    private Participante participante;

    @Column(nullable = false)
    private LocalDateTime dataInscricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusInscricao status =  StatusInscricao.ATIVA;
}
