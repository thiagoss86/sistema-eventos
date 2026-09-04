package com.eventos.sistema.sistema_eventos.evento.dto;

import com.eventos.sistema.sistema_eventos.evento.entity.StatusEvento;

import java.time.LocalDateTime;

public record EventoResponse(
        Long id,
        String nome,
        String descricao,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        String local,
        Integer capacidade,
        StatusEvento status
) {
}
