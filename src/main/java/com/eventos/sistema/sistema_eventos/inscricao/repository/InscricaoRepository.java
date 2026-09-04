package com.eventos.sistema.sistema_eventos.inscricao.repository;

import com.eventos.sistema.sistema_eventos.inscricao.entity.Inscricao;
import com.eventos.sistema.sistema_eventos.inscricao.entity.StatusInscricao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

    Optional<Inscricao> findByEventoIdAndParticipanteId(Long eventoId, Long participanteId);

    List<Inscricao> findByParticipanteId(Long participanteId);

    List<Inscricao> findByEventoId(Long eventoId);

    long countByEventoIdAndStatus(Long eventoId, StatusInscricao status);
}
