package com.eventos.sistema.sistema_eventos.notificacao.controller;

import com.eventos.sistema.sistema_eventos.notificacao.dto.NotificacaoRequest;
import com.eventos.sistema.sistema_eventos.notificacao.service.NotificacaoIntegrationService;
import com.eventos.sistema.sistema_eventos.shared.exception.ServicoIndisponivelException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificacaoController.class)
class NotificacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private NotificacaoIntegrationService notificacaoIntegrationService;

    @Test
    void deveRetornar503QuandoServicoDeNotificacaoEstiverIndisponivel()
            throws Exception {

        var request = new NotificacaoRequest(
                2L,
                com.eventos.sistema.sistema_eventos.notificacao.entity.TipoNotificacao.EMAIL,
                "Teste de indisponibilidade"
        );

        when(notificacaoIntegrationService.criar(any(NotificacaoRequest.class)))
                .thenThrow(
                        new ServicoIndisponivelException(
                                "O serviço de notificação está indisponivel."
                        )
                );

        mockMvc.perform(
                        post("/notificacoes")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isServiceUnavailable());
    }
}
