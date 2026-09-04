package com.eventos.sistema.sistema_eventos.inscricao.service;

import com.eventos.sistema.sistema_eventos.evento.entity.Evento;
import com.eventos.sistema.sistema_eventos.evento.entity.StatusEvento;
import com.eventos.sistema.sistema_eventos.evento.repository.EventoRepository;
import com.eventos.sistema.sistema_eventos.inscricao.dto.InscricaoRequest;
import com.eventos.sistema.sistema_eventos.inscricao.dto.InscricaoResponse;
import com.eventos.sistema.sistema_eventos.inscricao.entity.Inscricao;
import com.eventos.sistema.sistema_eventos.inscricao.entity.StatusInscricao;
import com.eventos.sistema.sistema_eventos.inscricao.repository.InscricaoRepository;
import com.eventos.sistema.sistema_eventos.participante.entity.Participante;
import com.eventos.sistema.sistema_eventos.participante.repository.ParticipanteRepository;
import com.eventos.sistema.sistema_eventos.shared.exception.RecursoNaoEncontradoException;
import com.eventos.sistema.sistema_eventos.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InscricaoService {

    private final InscricaoRepository inscricaoRepository;
    private final EventoRepository eventoRepository;
    private final ParticipanteRepository participanteRepository;

    @Transactional
    public InscricaoResponse criar(InscricaoRequest request) {

        Evento evento = buscarEvento(request.eventoId());
        Participante participante = buscarParticipante(request.participanteId());

        validarEventoDisponivel(evento);

        var inscricaoExistente = inscricaoRepository.findByEventoIdAndParticipanteId(
                request.eventoId(),
                participante.getId());

        if (inscricaoExistente.isPresent()) {

            Inscricao inscricao = inscricaoExistente.get();

            if (inscricao.getStatus() == StatusInscricao.ATIVA) {
                throw new RegraNegocioException(
                        "O participante já está inscrito neste evento."
                );
            }

            validarCapacidade(evento);

            inscricao.setDataInscricao(LocalDateTime.now());
            inscricao.setStatus(StatusInscricao.ATIVA);

            return toResponse(inscricao);
        }


        validarEventoDisponivel(evento);

        Inscricao inscricao = Inscricao.builder()
                .evento(evento)
                .participante(participante)
                .dataInscricao(LocalDateTime.now())
                .status(StatusInscricao.ATIVA)
                .build();

        Inscricao inscricaoSalva = inscricaoRepository.save(inscricao);

        return toResponse(inscricaoSalva);
    }

    @Transactional(readOnly = true)
    public List<InscricaoResponse> listarTodos() {

        return inscricaoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InscricaoResponse buscarPorId(Long id) {

        Inscricao inscricao = buscarEntidadePorId(id);

        return toResponse(inscricao);
    }

    @Transactional(readOnly = true)
    public List<InscricaoResponse> buscarPorEvento(Long eventoId) {

        buscarEvento(eventoId);

        return inscricaoRepository.findByEventoId(eventoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InscricaoResponse> buscarPorParticipante(Long participanteId) {

        buscarParticipante(participanteId);

        return inscricaoRepository.findByParticipanteId(participanteId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void cancelar(Long id) {

        Inscricao inscricao = buscarEntidadePorId(id);

        if (inscricao.getStatus() == StatusInscricao.CANCELADA) {
            throw new RegraNegocioException(
                    "A inscrição já está cancelada"
            );
        }

        inscricao.setStatus(StatusInscricao.CANCELADA);
    }

    private InscricaoResponse toResponse(Inscricao inscricao) {

        return new InscricaoResponse(
                inscricao.getId(),
                inscricao.getEvento().getId(),
                inscricao.getEvento().getNome(),
                inscricao.getParticipante().getId(),
                inscricao.getParticipante().getNome(),
                inscricao.getDataInscricao(),
                inscricao.getStatus()
        );
    }

    private Inscricao buscarEntidadePorId(Long id) {

        return inscricaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Inscrição não encontrada: " + id
                ));
    }

    private Participante buscarParticipante(Long id) {

        return participanteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Participante não encontrado."
                ));
    }

    private Evento buscarEvento(Long id) {

        return eventoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Evento não encontrado."
                        ));
    }

    private void validarCapacidade(Evento evento) {
        long inscricoesAtivas = inscricaoRepository.countByEventoIdAndStatus(
                evento.getId(),
                StatusInscricao.ATIVA
        );

        if (inscricoesAtivas >= evento.getCapacidade()) {
            throw new RegraNegocioException(
                    "O evento atingiu sua capacidade máxima."
            );
        }
    }

    private void validarEventoDisponivel(Evento evento) {

        if (evento.getStatus() != StatusEvento.ABERTO) {
            throw new RegraNegocioException(
                    "Não é possível realizar inscrição em um evento" +
                            "que não está aberto."
            );
        }
    }
}
