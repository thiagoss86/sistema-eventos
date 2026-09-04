package com.eventos.sistema.sistema_eventos.participante.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "participantes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_participantes_email",
                        columnNames = "email"
                )
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Participante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefone;
}
