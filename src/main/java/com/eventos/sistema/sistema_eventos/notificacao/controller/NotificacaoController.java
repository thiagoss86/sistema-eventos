package com.eventos.sistema.sistema_eventos.notificacao.controller;

import com.eventos.sistema.sistema_eventos.notificacao.dto.NotificacaoRequest;
import com.eventos.sistema.sistema_eventos.notificacao.dto.NotificacaoResponse;
import com.eventos.sistema.sistema_eventos.notificacao.entity.StatusNotificacao;
import com.eventos.sistema.sistema_eventos.notificacao.entity.TipoNotificacao;
import com.eventos.sistema.sistema_eventos.notificacao.service.NotificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
@Tag(
        name = "Notificações",
        description = "Operações relacionadas ao gerenciamento de notificações"
)
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @PostMapping
    @Operation(
            summary = "Criar notificação",
            description = "Cria uma nova notificação com status inicial PENDENTE"
    )
    public ResponseEntity<NotificacaoResponse> criar(
            @Valid @RequestBody NotificacaoRequest request
    ) {

        NotificacaoResponse response = notificacaoService.criar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @Operation(
            summary = "Listar notificações",
            description = "Retorna todas as notificações cadastradas"
    )
    public ResponseEntity<List<NotificacaoResponse>> listarTodas() {

        return ResponseEntity.ok(
                notificacaoService.buscarTodos()
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar notificação por ID",
            description = "Retorna uma notificação específica pelo seu identificador"
    )
    public ResponseEntity<NotificacaoResponse> buscarPorId(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                notificacaoService.buscarPorId(id)
        );
    }

    @GetMapping("/buscar/participantes/{participanteId}")
    @Operation(
            summary = "Buscar notificações por participante",
            description = "Retorna todas as notificações de um participante"
    )
    public ResponseEntity<List<NotificacaoResponse>> buscarPorParticipantes(
            @PathVariable Long participanteId
    ) {
        return ResponseEntity.ok(
                notificacaoService.buscarPorParticipante(participanteId)
        );
    }

    @GetMapping("/buscar/status")
    @Operation(
            summary = "Buscar notificações por status",
            description = "Retorna notificações filtradas pelo status informado"
    )
    public ResponseEntity<List<NotificacaoResponse>> buscarPorStatus(
            @RequestParam StatusNotificacao status
    ) {
        return ResponseEntity.ok(
                notificacaoService.buscarPorStatus(status)
        );
    }

    @GetMapping("/buscar/tipo")
    @Operation(
            summary = "Buscar notificações por tipo",
            description = "Retorna notificações filtradas pelo tipo informado"
    )
    public ResponseEntity<List<NotificacaoResponse>> buscarPorTipo(
            @RequestParam TipoNotificacao tipo
    ) {
        return ResponseEntity.ok(
                notificacaoService.buscarPorTipoNotificacao(tipo)
        );
    }

    @PatchMapping("/{id}/enviada")
    @Operation(
            summary = "Marcar notificação como enviada",
            description = "Altera o status da notificação para ENVIADA"
    )
    public ResponseEntity<NotificacaoResponse> marcarComoEnviada(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                notificacaoService.marcarComoEnviada(id)
        );
    }

    @PatchMapping("{id}/falha")
    @Operation(
            summary = "Marcar notificação como falha",
            description = "Altera o status da notificação para FALHA"
    )
    public ResponseEntity<NotificacaoResponse> marcarComoFalha(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                notificacaoService.marcarComoFalha(id)
        );
    }
}
