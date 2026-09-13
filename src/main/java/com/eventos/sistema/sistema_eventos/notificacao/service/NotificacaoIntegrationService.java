package com.eventos.sistema.sistema_eventos.notificacao.service;

import com.eventos.sistema.sistema_eventos.notificacao.client.NotificacaoClient;
import com.eventos.sistema.sistema_eventos.notificacao.dto.NotificacaoRequest;
import com.eventos.sistema.sistema_eventos.notificacao.dto.NotificacaoResponse;
import com.eventos.sistema.sistema_eventos.shared.exception.ServicoIndisponivelException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacaoIntegrationService {

    private final NotificacaoClient notificacaoClient;

    private static final String MSG_SERVICO_INDISPONIVEL = "O serviço de notificação está indisponivel.";

    public NotificacaoResponse criar(NotificacaoRequest notificacaoRequest) {
        try {
            return notificacaoClient.criar(notificacaoRequest);
        } catch (FeignException  e) {
            throw new ServicoIndisponivelException(
                    MSG_SERVICO_INDISPONIVEL,
                    e
            );
        }
    }

    public List<NotificacaoResponse> buscarTodos() {
        try {
            return notificacaoClient.listarTodos();
        } catch (FeignException  e) {
            throw new ServicoIndisponivelException(
                    MSG_SERVICO_INDISPONIVEL,
                    e
            );
        }
    }

    public NotificacaoResponse buscarPorId(Long id) {
        try {
            return notificacaoClient.buscarPorId(id);
        } catch (FeignException  e) {
            throw new ServicoIndisponivelException(
                    MSG_SERVICO_INDISPONIVEL,
                    e
            );
        }
    }

    public NotificacaoResponse marcarComEnviada(Long id) {
        try {
            return notificacaoClient.marcarComoEnviada(id);
        } catch (FeignException  e) {
            throw new ServicoIndisponivelException(
                    MSG_SERVICO_INDISPONIVEL,
                    e
            );
        }
    }

    public NotificacaoResponse marcarComFalha(Long id) {
        try {
            return notificacaoClient.marcarComoFalha(id);
        } catch (FeignException  e) {
            throw new ServicoIndisponivelException(
                    MSG_SERVICO_INDISPONIVEL,
                    e
            );
        }
    }
}
