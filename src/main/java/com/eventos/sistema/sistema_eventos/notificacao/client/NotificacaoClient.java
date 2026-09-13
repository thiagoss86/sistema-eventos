package com.eventos.sistema.sistema_eventos.notificacao.client;

import com.eventos.sistema.sistema_eventos.notificacao.dto.NotificacaoRequest;
import com.eventos.sistema.sistema_eventos.notificacao.dto.NotificacaoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(
        name = "notificacao-serveice",
        url = "${services.notificacao.url}",
        configuration = NotificacaoFeignConfig.class
)
public interface NotificacaoClient {

    @PostMapping("/notificacoes")
    NotificacaoResponse criar(NotificacaoRequest request);

    @GetMapping("/notificacoes")
    List<NotificacaoResponse> listarTodos();

    @GetMapping("/notificacoes/{id}")
    NotificacaoResponse buscarPorId(@PathVariable Long id);

    @PatchMapping("/notificacoes/{id}/enviada")
    NotificacaoResponse marcarComoEnviada(@PathVariable Long id);

    @PatchMapping("/notificacoes/{id}/falha")
    NotificacaoResponse marcarComoFalha(@PathVariable Long id);
}
