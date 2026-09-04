package com.eventos.sistema.sistema_eventos.evento.controller;

import com.eventos.sistema.sistema_eventos.evento.dto.EventoRequest;
import com.eventos.sistema.sistema_eventos.evento.dto.EventoResponse;
import com.eventos.sistema.sistema_eventos.evento.entity.StatusEvento;
import com.eventos.sistema.sistema_eventos.evento.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/eventos")
@RequiredArgsConstructor
@Tag(
        name = "Eventos",
        description = "Operações relacionadas ao gerenciamento de eventos"
)
public class EventoController {

    private final EventoService eventoService;

    @PostMapping
    @Operation(
            summary = "Criar evento",
            description = "Cria um novo evento com status inicial ABERTO"
    )
    public ResponseEntity<EventoResponse> criar(
            @Valid @RequestBody EventoRequest request
    ) {

        EventoResponse response = eventoService.criar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @Operation(
            summary = "Listar eventos",
            description = "Retorna todos os eventos cadastrados"
    )
    public ResponseEntity<List<EventoResponse>> listarTodos() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar evento por ID",
            description = "Retorna um evento específico pelo seu identificador"
    )
    public ResponseEntity<EventoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }

    @GetMapping("buscar/nome")
    @Operation(
            summary = "Buscar eventos por nome",
            description = "Retorna eventos cujo nome contém o texto informado"
    )
    public ResponseEntity<List<EventoResponse>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(eventoService.buscarPorNome(nome));
    }

    @GetMapping("buscar/status")
    @Operation(
            summary = "Buscar eventos por status",
            description = "Retorna eventos filtrados pelo status informado"
    )
    public ResponseEntity<List<EventoResponse>> buscarPorStatus(@RequestParam StatusEvento status) {
        return ResponseEntity.ok(eventoService.buscarPorStatus(status));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar evento",
            description = "Atualiza os dados de um evento existente"
    )
    public ResponseEntity<EventoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody EventoRequest request) {

        return ResponseEntity.ok(eventoService.atualizar(id, request));
    }

    @PatchMapping("{id}/cancelar")
    @Operation(
            summary = "Cancelar evento",
            description = "Cancela um evento que ainda pode ser cancelado"
    )
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id) {

        eventoService.cancelar(id);

        return ResponseEntity.noContent().build();
    }
}
