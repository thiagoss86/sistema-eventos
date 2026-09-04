package com.eventos.sistema.sistema_eventos.evento.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventoRequest(
        @NotBlank(message = "O nome do evento é obrigatório.")
        String nome,

        @NotBlank(message = "A descrição do evento é obrigatória.")
        String descricao,

        @NotNull(message = "A data de inicio é obrigatória.")
        LocalDateTime dataInicio,

        @NotNull(message = "A data de término é obrigatória.")
        LocalDateTime dataFim,

        @NotBlank(message = "O local do evento é obrigatório.")
        String local,

        @NotNull(message = "A capacidade do evento é obrigatória.")
        @Min(value = 1, message = "A capacidade do evento deve ser maior que zero")
        Integer capacidade
) {
}
