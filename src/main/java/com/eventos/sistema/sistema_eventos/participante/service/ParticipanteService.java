package com.eventos.sistema.sistema_eventos.participante.service;

import com.eventos.sistema.sistema_eventos.participante.dto.ParticipanteRequest;
import com.eventos.sistema.sistema_eventos.participante.dto.ParticipanteResponse;
import com.eventos.sistema.sistema_eventos.participante.entity.Participante;
import com.eventos.sistema.sistema_eventos.participante.repository.ParticipanteRepository;
import com.eventos.sistema.sistema_eventos.shared.exception.RecursoNaoEncontradoException;
import com.eventos.sistema.sistema_eventos.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipanteService {

    private final ParticipanteRepository participanteRepository;

    @Transactional
    public ParticipanteResponse criar(ParticipanteRequest request) {

        validarEmailDisponivel(request.email());

        Participante participante = Participante.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .build();

        Participante participanteSalvo = participanteRepository.save(participante);

        return toResponse(participanteSalvo);

    }

    @Transactional(readOnly = true)
    public List<ParticipanteResponse> listarTodos() {
        return participanteRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ParticipanteResponse buscarPorId(Long id) {
        Participante participante = buscarEntidadePorId(id);

        return toResponse(participante);
    }

    @Transactional(readOnly = true)
    public List<ParticipanteResponse> listarPorNome(String nome) {
        return participanteRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ParticipanteResponse buscarPorEmail(String email) {
        Participante participante = participanteRepository
                .findByEmailContainingIgnoreCase(email)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Participante não encontrado para o e-mail informado."
                        ));

        return toResponse(participante);
    }

    @Transactional
    public ParticipanteResponse atualizar(
            Long id,
            ParticipanteRequest request) {

        Participante participante = buscarEntidadePorId(id);

        validarEmailDisponivelParaAtualizar(
                participante,
                request.email());

        participante.setNome(request.nome());
        participante.setEmail(request.email());
        participante.setTelefone(request.telefone());

        return toResponse(participante);
    }

    @Transactional
    public void excluir(Long id) {
        Participante participante = buscarEntidadePorId(id);

        participanteRepository.delete(participante);
    }

    private Participante buscarEntidadePorId(Long id) {
        return participanteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Participante não encontrado: " + id
                ));
    }

    private void validarEmailDisponivel(String email) {
        if (participanteRepository.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException(
                    "Já existe um participante com esse e-mail informado.");
        }
    }

    private void validarEmailDisponivelParaAtualizar(Participante participante, String email) {
        participanteRepository.findByEmailContainingIgnoreCase(email)
                .ifPresent(participanteEncontrado -> {

                    if (!participanteEncontrado.getEmail().equals(participante.getEmail())) {
                        throw new RegraNegocioException(
                                "Já existe outro participante cadastrado com o e-mail informado.");
                    }
                });
    }

    private ParticipanteResponse toResponse(Participante participante) {
        return new ParticipanteResponse(
                participante.getId(),
                participante.getNome(),
                participante.getEmail(),
                participante.getTelefone());
    }
}
