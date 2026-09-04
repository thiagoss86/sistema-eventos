package com.eventos.sistema.sistema_eventos.shared.exception.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErroValidacaoResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        Map<String, String> detalhes,
        String requestStatusId
) {
}
