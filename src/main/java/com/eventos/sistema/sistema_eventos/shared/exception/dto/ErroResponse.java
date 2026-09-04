package com.eventos.sistema.sistema_eventos.shared.exception.dto;

import java.time.LocalDateTime;

public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String requestStatusId
) {
}
