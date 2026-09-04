package com.eventos.sistema.sistema_eventos.evento.service;

import com.eventos.sistema.sistema_eventos.evento.dto.EventoRequest;
import com.eventos.sistema.sistema_eventos.evento.dto.EventoResponse;
import com.eventos.sistema.sistema_eventos.evento.entity.Evento;
import com.eventos.sistema.sistema_eventos.evento.entity.StatusEvento;
import com.eventos.sistema.sistema_eventos.evento.repository.EventoRepository;
import com.eventos.sistema.sistema_eventos.shared.exception.RecursoNaoEncontradoException;
import com.eventos.sistema.sistema_eventos.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    @Transactional
    public EventoResponse criar(EventoRequest request) {
        validarPeriodo(request);

        Evento evento = Evento.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .dataInicio(request.dataInicio())
                .dataFim(request.dataFim())
                .local(request.local())
                .capacidade(request.capacidade())
                .status(StatusEvento.ABERTO)
                .build();

        Evento eventoSalvo = eventoRepository.save(evento);

        return toResponse(eventoSalvo);
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listarTodos() {
        return eventoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventoResponse buscarPorId(Long id) {
        Evento evento = buscarEntidadePorId(id);
        return toResponse(evento);
    }

    @Transactional
    public EventoResponse atualizar(Long id, EventoRequest request) {
        Evento evento = buscarEntidadePorId(id);

        validarEventoNaoCancelado(evento);
        validarPeriodo(request);

        evento.setNome(request.nome());
        evento.setDescricao(request.descricao());
        evento.setDataInicio(request.dataInicio());
        evento.setDataFim(request.dataFim());
        evento.setLocal(request.local());
        evento.setCapacidade(request.capacidade());

        return toResponse(evento);
    }

    @Transactional
    public void cancelar(Long id) {
        Evento evento = buscarEntidadePorId(id);

        validarEventoNaoCancelado(evento);

        if (evento.getStatus() == StatusEvento.ENCERRADO) {
            throw new RegraNegocioException("Não é possível cancelar um evento encerrado");
        }

        evento.setStatus(StatusEvento.CANCELADO);
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> buscarPorNome(String nome) {
        return eventoRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> buscarPorStatus(StatusEvento status) {
        return eventoRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Evento buscarEntidadePorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Evento não encontrado: " + id));
    }

    private void validarPeriodo(EventoRequest request) {
        if (request.dataFim().isBefore(request.dataInicio())) {
            throw new RegraNegocioException(
                    "A data de término não pode ser anterior à data de início."
            );
        }
    }

    private void validarEventoNaoCancelado(Evento evento) {
        if (evento.getStatus() == StatusEvento.CANCELADO) {
            throw new RegraNegocioException(
                    "Não é possível alterar um evento cancelado"
            );
        }
    }

    private EventoResponse toResponse(Evento evento) {
        return new EventoResponse(
                evento.getId(),
                evento.getNome(),
                evento.getDescricao(),
                evento.getDataInicio(),
                evento.getDataFim(),
                evento.getLocal(),
                evento.getCapacidade(),
                evento.getStatus()
        );
    }
}
