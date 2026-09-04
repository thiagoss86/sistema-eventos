package com.eventos.sistema.sistema_eventos.participante.repository;

import com.eventos.sistema.sistema_eventos.participante.entity.Participante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipanteRepository extends JpaRepository<Participante, Long> {

    List<Participante> findByNomeContainingIgnoreCase(String nome);

    Optional<Participante> findByEmailContainingIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
