package com.eventos.sistema.sistema_eventos.inscricao.controller;

import com.eventos.sistema.sistema_eventos.inscricao.dto.InscricaoRequest;
import com.eventos.sistema.sistema_eventos.inscricao.dto.InscricaoResponse;
import com.eventos.sistema.sistema_eventos.inscricao.service.InscricaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inscricoes")
@RequiredArgsConstructor
@Tag(
        name = "Inscrições",
        description = "Operações relacionadas ao gerenciamento de inscrições"
)
public class InscricaoController {

    private final InscricaoService inscricaoService;

    @PostMapping
    @Operation(
            summary = "Criar inscrição",
            description = "Cria uma nova inscrição ou reativa uma inscrição cancelada"
    )
    public ResponseEntity<InscricaoResponse> criar(
            @Valid @RequestBody InscricaoRequest request
    ) {

        InscricaoResponse response = inscricaoService.criar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }

    @GetMapping
    @Operation(
            summary = "Listar inscrições",
            description = "Retorna todas as inscrições cadastradas"
    )
    public ResponseEntity<List<InscricaoResponse>> listarTodos() {
        return ResponseEntity.ok(inscricaoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar inscrição por ID",
            description = "Retorna uma inscrição específica pelo seu identificador"
    )
    public ResponseEntity<InscricaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(inscricaoService.buscarPorId(id));
    }

    @GetMapping("/buscar/evento/{eventoId}")
    @Operation(
            summary = "Buscar inscrições por evento",
            description = "Retorna todas as inscrições relacionadas a um evento"
    )
    public ResponseEntity<List<InscricaoResponse>> buscarPorEventoId(@PathVariable Long eventoId) {
        return ResponseEntity.ok(inscricaoService.buscarPorEvento(eventoId));
    }

    @GetMapping("/buscar/participante/{participanteId}")
    @Operation(
            summary = "Buscar inscrições por participante",
            description = "Retorna todas as inscrições de um participante"
    )
    public ResponseEntity<List<InscricaoResponse>> buscarPorParticipanteId(@PathVariable Long participanteId) {
        return ResponseEntity.ok(inscricaoService.buscarPorParticipante(participanteId));
    }

    @DeleteMapping("/{id}/cancelar")
    @Operation(
            summary = "Cancelar inscrição",
            description = "Cancela uma inscrição existente"
    )
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        inscricaoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
