package com.eventos.sistema.sistema_eventos.participante.dto;

public record ParticipanteResponse(
        Long id,
        String nome,
        String email,
        String telefone) {
}
