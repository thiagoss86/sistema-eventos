package com.eventos.sistema.sistema_eventos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SistemaEventosApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemaEventosApplication.class, args);
	}

}
