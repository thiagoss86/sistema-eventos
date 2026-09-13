package com.eventos.sistema.sistema_eventos.notificacao.controller;

import com.eventos.sistema.sistema_eventos.notificacao.dto.NotificacaoRequest;
import com.eventos.sistema.sistema_eventos.notificacao.dto.NotificacaoResponse;
import com.eventos.sistema.sistema_eventos.notificacao.service.NotificacaoIntegrationService;
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

    private final NotificacaoIntegrationService notificacaoIntegrationService;

    @PostMapping
    @Operation(
            summary = "Criar notificação",
            description = "Cria uma nova notificação com status inicial PENDENTE"
    )
    public ResponseEntity<NotificacaoResponse> criar(
            @Valid @RequestBody NotificacaoRequest request
    ) {

        NotificacaoResponse response = notificacaoIntegrationService.criar(request);

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
                notificacaoIntegrationService.buscarTodos()
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
                notificacaoIntegrationService.buscarPorId(id)
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
                notificacaoIntegrationService.marcarComEnviada(id)
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
                notificacaoIntegrationService.marcarComFalha(id)
        );
    }
}
