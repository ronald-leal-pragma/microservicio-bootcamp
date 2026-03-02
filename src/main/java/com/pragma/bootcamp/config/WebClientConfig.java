package com.pragma.bootcamp.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestión de Bootcamp")
                        .version("1.0")
                        .description("Servicios reactivos para la gestión de tecnologías, capacidades y franquicias en el Bootcamp"));
    }
}
