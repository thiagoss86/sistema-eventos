package com.eventos.sistema.sistema_eventos.inscricao.dto;

import com.eventos.sistema.sistema_eventos.inscricao.entity.StatusInscricao;

import java.time.LocalDateTime;

public record InscricaoResponse(
        Long id,
        Long eventoId,
        String eventoNome,
        Long participanteId,
        String participanteNome,
        LocalDateTime dataInscricao,
        StatusInscricao status
) {
}
