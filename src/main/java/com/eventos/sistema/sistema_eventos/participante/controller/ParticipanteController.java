package com.eventos.sistema.sistema_eventos.participante.controller;

import com.eventos.sistema.sistema_eventos.participante.dto.ParticipanteRequest;
import com.eventos.sistema.sistema_eventos.participante.dto.ParticipanteResponse;
import com.eventos.sistema.sistema_eventos.participante.service.ParticipanteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/participantes")
@RequiredArgsConstructor
@Tag(
        name = "Participantes",
        description = "Operações relacionadas ao gerenciamento de participantes"
)
public class ParticipanteController {

    private final ParticipanteService participanteService;

    @PostMapping
    @Operation(
            summary = "Criar participante",
            description = "Cadastra um novo participante no sistema"
    )
    public ResponseEntity<ParticipanteResponse> criar(
            @Valid @RequestBody ParticipanteRequest participanteRequest) {

        ParticipanteResponse response = participanteService.criar(participanteRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @Operation(
            summary = "Listar participantes",
            description = "Retorna todos os participantes cadastrados"
    )
    public ResponseEntity<List<ParticipanteResponse>> listarTodos() {
        return ResponseEntity.ok(participanteService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar participante por ID",
            description = "Retorna um participante específico pelo seu identificador"
    )
    public ResponseEntity<ParticipanteResponse> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(participanteService.buscarPorId(id));
    }

    @GetMapping("/buscar/nome")
    @Operation(
            summary = "Buscar participantes por nome",
            description = "Retorna participantes cujo nome contém o texto informado"
    )
    public ResponseEntity<List<ParticipanteResponse>> buscarPorNome(
            @RequestParam String nome) {
        return ResponseEntity.ok(participanteService.listarPorNome(nome));
    }

    @GetMapping("/buscar/email")
    @Operation(
            summary = "Buscar participante por e-mail",
            description = "Retorna um participante pelo endereço de e-mail"
    )
    public ResponseEntity<ParticipanteResponse> buscarPorEmail(
            @RequestParam String email) {
        return ResponseEntity.ok(participanteService.buscarPorEmail(email));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar participante",
            description = "Atualiza os dados de um participante existente"
    )
    public ResponseEntity<ParticipanteResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ParticipanteRequest participanteRequest) {
        return ResponseEntity.ok(participanteService.atualizar(id, participanteRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir participante",
            description = "Remove um participante do sistema"
    )
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {
        participanteService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
