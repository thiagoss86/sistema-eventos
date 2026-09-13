package com.eventos.sistema.sistema_eventos.notificacao.dto;

import com.eventos.sistema.sistema_eventos.notificacao.entity.TipoNotificacao;

public record NotificacaoRequest(
        Long participanteId,
        TipoNotificacao tipo,
        String mensagem
) {}