package com.eventos.sistema.sistema_eventos.shared.exception;

import com.eventos.sistema.sistema_eventos.shared.exception.dto.ErroResponse;
import com.eventos.sistema.sistema_eventos.shared.exception.dto.ErroValidacaoResponse;
import com.eventos.sistema.sistema_eventos.shared.filter.RequestStatusFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException ex,
            HttpServletRequest request) {

        return criarResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(
            RegraNegocioException ex,
            HttpServletRequest request) {

        return criarResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroValidacaoResponse> tratarValidacao(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> detalhes = new HashMap<>();

        ex.getBindingResult()
                .getAllErrors()
                .forEach((error) ->
                        detalhes.put(
                                error.getObjectName(),
                                error.getDefaultMessage()
                        )
                );

        ErroValidacaoResponse response = new ErroValidacaoResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Dados inválidos",
                detalhes,
                obterRequestStatusId(request));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    private ResponseEntity<ErroResponse> criarResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErroResponse response = new ErroResponse(
                LocalDateTime.now(),
                status.value(),
                message,
                obterRequestStatusId(request)
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    private String obterRequestStatusId(HttpServletRequest request) {
        return (String) request.getAttribute(
                RequestStatusFilter.REQUEST_STATUS_ID);
    }
}
