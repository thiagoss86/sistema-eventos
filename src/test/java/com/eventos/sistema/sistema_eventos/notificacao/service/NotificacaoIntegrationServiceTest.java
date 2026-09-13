package com.eventos.sistema.sistema_eventos.notificacao.service;

import com.eventos.sistema.sistema_eventos.notificacao.client.NotificacaoClient;
import com.eventos.sistema.sistema_eventos.notificacao.dto.NotificacaoRequest;
import com.eventos.sistema.sistema_eventos.notificacao.dto.NotificacaoResponse;
import com.eventos.sistema.sistema_eventos.notificacao.entity.StatusNotificacao;
import com.eventos.sistema.sistema_eventos.notificacao.entity.TipoNotificacao;
import com.eventos.sistema.sistema_eventos.shared.exception.ServicoIndisponivelException;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoIntegrationServiceTest {

    @Mock
    private NotificacaoClient notificacaoClient;

    @InjectMocks
    private NotificacaoIntegrationService notificacaoIntegrationService;

    @Test
    void deveCriarNotificacaoComSucesso() {

        var request = new NotificacaoRequest(
                2L,
                TipoNotificacao.EMAIL,
                "Teste de integração"
        );

        var response = new NotificacaoResponse(
                1L,
                2L,
                TipoNotificacao.EMAIL,
                "Teste de integração",
                LocalDateTime.now(),
                StatusNotificacao.PENDENTE
        );

        when(notificacaoClient.criar(request))
                .thenReturn(response);

        var resultado = notificacaoIntegrationService.criar(request);

        assertEquals(response, resultado);

        verify(notificacaoClient).criar(request);
    }

    @Test
    void deveLancarExcecaoQuandoServicoDeNotificacaoEstiverIndisponivel() {

        var request = new NotificacaoRequest(
                2L,
                TipoNotificacao.EMAIL,
                "Teste de indisponibilidade"
        );

        when(notificacaoClient.criar(request))
                .thenThrow(FeignException.errorStatus(
                        "POST /notificacoes",
                        feign.Response.builder()
                                .status(503)
                                .reason("Service Unavailable")
                                .request(
                                        feign.Request.create(
                                                feign.Request.HttpMethod.POST,
                                                "http://localhost:8081/notificacoes",
                                                java.util.Collections.emptyMap(),
                                                null,
                                                null,
                                                null
                                        )
                                )
                                .build()
                ));

        var exception = org.junit.jupiter.api.Assertions.assertThrows(
                ServicoIndisponivelException.class,
                () -> notificacaoIntegrationService.criar(request)
        );

        assertEquals(
                "O serviço de notificação está indisponivel.",
                exception.getMessage()
        );

        verify(notificacaoClient).criar(request);
    }
}
