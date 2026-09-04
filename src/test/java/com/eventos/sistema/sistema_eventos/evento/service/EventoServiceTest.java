package com.eventos.sistema.sistema_eventos.evento.service;

import com.eventos.sistema.sistema_eventos.evento.dto.EventoRequest;
import com.eventos.sistema.sistema_eventos.evento.dto.EventoResponse;
import com.eventos.sistema.sistema_eventos.evento.entity.Evento;
import com.eventos.sistema.sistema_eventos.evento.entity.StatusEvento;
import com.eventos.sistema.sistema_eventos.evento.repository.EventoRepository;
import com.eventos.sistema.sistema_eventos.shared.exception.RegraNegocioException;
import com.eventos.sistema.sistema_eventos.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @InjectMocks
    private EventoService eventoService;

    private EventoRequest eventoRequest;
    private Evento evento;

    @BeforeEach
    void setUp() {
        eventoRequest = new EventoRequest(
                "Evento de Tecnologia",
                "Evento sobre tecnologia e inovação",
                LocalDateTime.of(2026, 10, 10, 10, 0),
                LocalDateTime.of(2026, 10, 10, 18, 0),
                "Rio de Janeiro",
                100
        );

        evento = Evento.builder()
                .id(1L)
                .nome("Evento de Tecnologia")
                .descricao("Evento sobre tecnologia e inovação")
                .dataInicio(eventoRequest.dataInicio())
                .dataFim(eventoRequest.dataFim())
                .local("Rio de Janeiro")
                .capacidade(100)
                .status(StatusEvento.ABERTO)
                .build();
    }

    @Test
    void deveCriarEventoComSucesso() {
        when(eventoRepository.save(any(Evento.class)))
                .thenReturn(evento);

        EventoResponse response = eventoService.criar(eventoRequest);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Evento de Tecnologia", response.nome());
        assertEquals(StatusEvento.ABERTO, response.status());

        verify(eventoRepository).save(any(Evento.class));
    }

    @Test
    void deveLancarExcecaoQuandoPeriodoForInvalido() {
        EventoRequest requestInvalido = new EventoRequest(
                "Evento de Tecnologia",
                "Evento sobre tecnologia e inovação",
                LocalDateTime.of(2026, 10, 10, 18, 0),
                LocalDateTime.of(2026, 10, 10, 10, 0),
                "Rio de Janeiro",
                100
        );

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> eventoService.criar(requestInvalido)
        );

        assertEquals(
                "A data de término não pode ser anterior à data de início.",
                exception.getMessage()
        );

        verify(eventoRepository, never()).save(any(Evento.class));
    }

    @Test
    void deveBuscarEventoPorIdComSucesso() {
        when(eventoRepository.findById(1L))
                .thenReturn(Optional.of(evento));

        EventoResponse response = eventoService.buscarPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Evento de Tecnologia", response.nome());

        verify(eventoRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoQuandoEventoNaoForEncontrado() {
        when(eventoRepository.findById(999L))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> eventoService.buscarPorId(999L)
        );

        assertEquals(
                "Evento não encontrado: 999",
                exception.getMessage()
        );

        verify(eventoRepository).findById(999L);
    }

    @Test
    void deveListarTodosOsEventos() {
        Evento segundoEvento = Evento.builder()
                .id(2L)
                .nome("Outro Evento")
                .descricao("Outro evento de teste")
                .dataInicio(LocalDateTime.of(2026, 11, 10, 10, 0))
                .dataFim(LocalDateTime.of(2026, 11, 10, 18, 0))
                .local("São Paulo")
                .capacidade(200)
                .status(StatusEvento.ABERTO)
                .build();

        when(eventoRepository.findAll())
                .thenReturn(List.of(evento, segundoEvento));

        List<EventoResponse> response = eventoService.listarTodos();

        assertEquals(2, response.size());
        assertEquals("Evento de Tecnologia", response.get(0).nome());
        assertEquals("Outro Evento", response.get(1).nome());

        verify(eventoRepository).findAll();
    }

    @Test
    void deveAtualizarEventoComSucesso() {
        when(eventoRepository.findById(1L))
                .thenReturn(Optional.of(evento));

        EventoRequest requestAtualizacao = new EventoRequest(
                "Evento de Tecnologia Atualizado",
                "Nova descrição",
                LocalDateTime.of(2026, 10, 11, 10, 0),
                LocalDateTime.of(2026, 10, 11, 19, 0),
                "Niterói",
                150
        );

        EventoResponse response =
                eventoService.atualizar(1L, requestAtualizacao);

        assertEquals("Evento de Tecnologia Atualizado", response.nome());
        assertEquals("Nova descrição", response.descricao());
        assertEquals("Niterói", response.local());
        assertEquals(150, response.capacidade());

        verify(eventoRepository).findById(1L);
    }

    @Test
    void naoDeveAtualizarEventoCancelado() {
        evento.setStatus(StatusEvento.CANCELADO);

        when(eventoRepository.findById(1L))
                .thenReturn(Optional.of(evento));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> eventoService.atualizar(1L, eventoRequest)
        );

        assertEquals(
                "Não é possível alterar um evento cancelado",
                exception.getMessage()
        );
    }

    @Test
    void deveCancelarEventoComSucesso() {
        when(eventoRepository.findById(1L))
                .thenReturn(Optional.of(evento));

        eventoService.cancelar(1L);

        assertEquals(StatusEvento.CANCELADO, evento.getStatus());

        verify(eventoRepository).findById(1L);
    }

    @Test
    void naoDeveCancelarEventoEncerrado() {
        evento.setStatus(StatusEvento.ENCERRADO);

        when(eventoRepository.findById(1L))
                .thenReturn(Optional.of(evento));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> eventoService.cancelar(1L)
        );

        assertEquals(
                "Não é possível cancelar um evento encerrado",
                exception.getMessage()
        );

        assertEquals(StatusEvento.ENCERRADO, evento.getStatus());
    }

    @Test
    void deveBuscarEventosPorNome() {
        when(eventoRepository.findByNomeContainingIgnoreCase(
                "tecnologia"
        )).thenReturn(List.of(evento));

        List<EventoResponse> response =
                eventoService.buscarPorNome("tecnologia");

        assertEquals(1, response.size());
        assertEquals("Evento de Tecnologia", response.getFirst().nome());

        verify(eventoRepository)
                .findByNomeContainingIgnoreCase("tecnologia");
    }

    @Test
    void deveBuscarEventosPorStatus() {
        when(eventoRepository.findByStatus(StatusEvento.ABERTO))
                .thenReturn(List.of(evento));

        List<EventoResponse> response =
                eventoService.buscarPorStatus(StatusEvento.ABERTO);

        assertEquals(1, response.size());
        assertEquals(StatusEvento.ABERTO, response.getFirst().status());

        verify(eventoRepository)
                .findByStatus(StatusEvento.ABERTO);
    }
}
