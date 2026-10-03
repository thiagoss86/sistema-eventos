package com.eventos.sistema.sistema_eventos.notificacao.messaging;

import com.eventos.sistema.sistema_eventos.notificacao.dto.NotificacaoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.eventos.sistema.sistema_eventos.config.RabbitMqConfig.NOTIFICACAO_EXCHANGE;
import static com.eventos.sistema.sistema_eventos.config.RabbitMqConfig.NOTIFICACAO_ROUTING_KEY;

@Component
@RequiredArgsConstructor
public class NotificacaoProducer {

    private final RabbitTemplate rabbitTemplate;

    public void enviar(NotificacaoRequest notificacaoRequest){
        rabbitTemplate.convertAndSend(
                NOTIFICACAO_EXCHANGE,
                NOTIFICACAO_ROUTING_KEY,
                notificacaoRequest
        );
    }
}
