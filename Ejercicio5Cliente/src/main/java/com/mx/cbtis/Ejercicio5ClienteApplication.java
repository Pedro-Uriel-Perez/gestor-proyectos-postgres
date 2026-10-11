package com.mx.cbtis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Ejercicio5ClienteApplication {

	public static void main(String[] args) {
		SpringApplication.run(Ejercicio5ClienteApplication.class, args);
	}

	/** Cliente HTTP con el que el frontend consume la API REST. */
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

}
