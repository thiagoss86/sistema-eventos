package com.eventos.sistema.sistema_eventos.notificacao.service;

import com.eventos.sistema.sistema_eventos.notificacao.dto.NotificacaoRequest;
import com.eventos.sistema.sistema_eventos.notificacao.dto.NotificacaoResponse;
import com.eventos.sistema.sistema_eventos.notificacao.entity.Notificacao;
import com.eventos.sistema.sistema_eventos.notificacao.entity.StatusNotificacao;
import com.eventos.sistema.sistema_eventos.notificacao.entity.TipoNotificacao;
import com.eventos.sistema.sistema_eventos.notificacao.repository.NotificacaoRepository;
import com.eventos.sistema.sistema_eventos.participante.entity.Participante;
import com.eventos.sistema.sistema_eventos.participante.repository.ParticipanteRepository;
import com.eventos.sistema.sistema_eventos.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final ParticipanteRepository participanteRepository;

    @Transactional
    public NotificacaoResponse criar(NotificacaoRequest request) {

        Participante participante = buscarParticipante(request.participanteId());

        Notificacao notificacao = Notificacao.builder()
                .participante(participante)
                .tipoNotificacao(request.tipo())
                .mensagem(request.mensagem())
                .data(LocalDateTime.now())
                .status(StatusNotificacao.PENDENTE)
                .build();

        Notificacao notificacaoSalva = notificacaoRepository.save(notificacao);

        return toResponse(notificacaoSalva);
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponse> buscarTodos() {
        return notificacaoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificacaoResponse buscarPorId(Long id) {
        Notificacao notificacao = buscarEntidadePorId(id);

        return toResponse(notificacao);
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponse> buscarPorParticipante(Long participanteId) {

        buscarParticipante(participanteId);

        return notificacaoRepository.findByParticipanteId(participanteId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponse> buscarPorStatus(StatusNotificacao status) {
        return notificacaoRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponse> buscarPorTipoNotificacao(TipoNotificacao tipoNotificacao) {
        return notificacaoRepository.findByTipoNotificacao(tipoNotificacao)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificacaoResponse marcarComoEnviada(Long id) {

        Notificacao notificacao = buscarEntidadePorId(id);

        if (notificacao.getStatus() == StatusNotificacao.ENVIADA) {
            throw new RegraNegocioException(
                    "A notificação já foi enviada."
            );
        }

        notificacao.setStatus(StatusNotificacao.ENVIADA);

        return toResponse(notificacao);
    }

    @Transactional
    public NotificacaoResponse marcarComoFalha(Long id) {

        Notificacao notificacao = buscarEntidadePorId(id);

        if (notificacao.getStatus() == StatusNotificacao.ENVIADA) {
            throw new RegraNegocioException(
                    "Não é possível marcar como falha uma notificação já enviada"
            );
        }

        notificacao.setStatus(StatusNotificacao.FALHA);

        return toResponse(notificacao);
    }

    private Notificacao buscarEntidadePorId(Long id) {

        return notificacaoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException(
                        "Notificação não encontrada: " + id
                ));
    }

    private Participante buscarParticipante(Long participanteId) {
        return participanteRepository.findById(participanteId)
                .orElseThrow(() -> new RegraNegocioException(
                        "Participante não encontrado: " + participanteId
                ));
    }

    private NotificacaoResponse toResponse(Notificacao notificacao) {

        return new NotificacaoResponse(
                notificacao.getId(),
                notificacao.getParticipante().getId(),
                notificacao.getParticipante().getNome(),
                notificacao.getTipoNotificacao(),
                notificacao.getMensagem(),
                notificacao.getData(),
                notificacao.getStatus()
        );
    }
}
