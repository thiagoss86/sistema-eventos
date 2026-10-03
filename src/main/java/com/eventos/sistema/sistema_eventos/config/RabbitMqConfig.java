package com.eventos.sistema.sistema_eventos.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String NOTIFICACAO_EXCHANGE = "eventos.notificacao.exchange";
    public static final String NOTIFICACAO_QUEUE = "eventos.notificacao.queue";
    public static final String NOTIFICACAO_ROUTING_KEY = "eventos.notificacao";

    @Bean
    public DirectExchange notificacaoExchange() {
        return new DirectExchange(NOTIFICACAO_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificacaoQueue() {
        return new Queue(NOTIFICACAO_QUEUE, true);
    }

    @Bean
    public Binding notificacaoBinding(
            Queue notificacaoQueue,
            DirectExchange notificacaoExchange) {

        return BindingBuilder
                .bind(notificacaoQueue)
                .to(notificacaoExchange)
                .with(NOTIFICACAO_ROUTING_KEY);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        rabbitAdmin.initialize();
        return rabbitAdmin;
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter messageConverter) {

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);

        return rabbitTemplate;
    }
}
