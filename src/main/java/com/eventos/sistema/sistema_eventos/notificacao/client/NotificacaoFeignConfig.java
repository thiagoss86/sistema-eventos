package com.eventos.sistema.sistema_eventos.notificacao.client;

import feign.Client;
import feign.okhttp.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificacaoFeignConfig {

    @Bean
    public Client feignClient() {
        return new OkHttpClient();
    }
}
