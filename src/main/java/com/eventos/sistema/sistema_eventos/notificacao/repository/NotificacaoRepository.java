package com.eventos.sistema.sistema_eventos.notificacao.repository;

import com.eventos.sistema.sistema_eventos.notificacao.entity.Notificacao;
import com.eventos.sistema.sistema_eventos.notificacao.entity.StatusNotificacao;
import com.eventos.sistema.sistema_eventos.notificacao.entity.TipoNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByParticipanteId(Long participanteId);

    List<Notificacao> findByStatus(StatusNotificacao status);

    List<Notificacao> findByTipoNotificacao(TipoNotificacao tipoNotificacao);
}
