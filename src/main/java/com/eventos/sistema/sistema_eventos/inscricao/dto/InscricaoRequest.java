package com.eventos.sistema.sistema_eventos.inscricao.dto;

import jakarta.validation.constraints.NotNull;

public record InscricaoRequest(

        @NotNull(message = "O evento é obrigatório.")
        Long eventoId,

        @NotNull(message = "O participante é obrigatório.")
        Long participanteId
) {
}
