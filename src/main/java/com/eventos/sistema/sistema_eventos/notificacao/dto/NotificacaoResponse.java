package com.eventos.sistema.sistema_eventos.notificacao.dto;

import com.eventos.sistema.sistema_eventos.notificacao.entity.StatusNotificacao;
import com.eventos.sistema.sistema_eventos.notificacao.entity.TipoNotificacao;

import java.time.LocalDateTime;

public record NotificacaoResponse(
        long id,
        long participanteId,
        String participanteNome,
        TipoNotificacao tipo,
        String mensagem,
        LocalDateTime data,
        StatusNotificacao status
) {
}
