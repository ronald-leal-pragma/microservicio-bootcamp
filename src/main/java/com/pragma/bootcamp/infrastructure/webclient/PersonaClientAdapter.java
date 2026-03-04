package com.pragma.bootcamp.infrastructure.webclient;

import com.pragma.bootcamp.domain.models.Persona;
import com.pragma.bootcamp.domain.ports.out.IPersonaServicePort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonaClientAdapter implements IPersonaServicePort {

    private static final String SERVICE_CLIENT = "persona-client";

    private final WebClient.Builder webClientBuilder;

    private WebClient client() {
        return webClientBuilder.baseUrl("http://localhost:8084").build();
    }


    @Override
    @CircuitBreaker(name = SERVICE_CLIENT, fallbackMethod = "findByIdFallback")
    public Mono<Persona> findById(Long id) {
        log.info("PersonaClient: Buscando persona por ID: {}", id);
        return client()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/persona/{id}").build(id))
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        log.warn("PersonaClient: Persona con ID {} no encontrada (404)", id);
                        return response.releaseBody().then(Mono.empty());
                    }
                    return response.bodyToMono(Persona.class);
                })
                .doOnSuccess(p -> log.info("PersonaClient: Persona encontrada: {}", p != null ? p.getNombre() : "null"));
    }
}
