package com.pragma.bootcamp.infrastructure.webclient;

import com.pragma.bootcamp.application.dtos.responses.CapacidadSimpleResponse;
import com.pragma.bootcamp.domain.ports.out.ICapacidadServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CapacidadClientAdapter implements ICapacidadServicePort {

    private final WebClient.Builder webClientBuilder;

    private WebClient client() {
        return webClientBuilder.baseUrl("http://localhost:8082").build();
    }

    @Override
    public Mono<Void> validateCapacidadExists(Long id) {
        return client()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/capacidad/{id}").build(id))
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, resp -> Mono.error(new IllegalArgumentException("Capacidad no encontrada: " + id)))
                .bodyToMono(Object.class)
                .then();
    }

    @Override
    public Flux<CapacidadSimpleResponse> getCapacidadesByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }

        String idsParam = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return client()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/capacidad/batch")
                        .queryParam("ids", idsParam)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<CapacidadSimpleResponse>>() {})
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Void> deleteCapacidad(Long id) {
        return client()
                .delete()
                .uri(uriBuilder -> uriBuilder.path("/api/capacidad/{id}").build(id))
                .retrieve()
                .bodyToMono(Object.class)
                .then()
                .onErrorResume(e -> {
                    // Si es 404, la capacidad ya no existe, no es un error crítico
                    return Mono.empty();
                });
    }
}
