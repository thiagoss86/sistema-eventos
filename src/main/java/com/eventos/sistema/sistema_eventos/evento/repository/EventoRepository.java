package com.eventos.sistema.sistema_eventos.evento.repository;

import com.eventos.sistema.sistema_eventos.evento.entity.Evento;
import com.eventos.sistema.sistema_eventos.evento.entity.StatusEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findByNomeContainingIgnoreCase(String nome);

    List<Evento> findByStatus(StatusEvento status);
}
