package com.eventos.sistema.sistema_eventos.notificacao.dto;

import com.eventos.sistema.sistema_eventos.notificacao.entity.TipoNotificacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificacaoRequest(
        @NotNull(message = "O participante é obrigatório")
        Long participanteId,

        @NotNull(message = "O tipo da notificação é obrigatório")
        TipoNotificacao tipo,

        @NotBlank(message = "A mensagem da notificação é obrigatória")
        @Size(
                max = 1000,
                message = "A mensagem deve ter no máximo 1000 caracteres"
        )
        String mensagem
) {


}
