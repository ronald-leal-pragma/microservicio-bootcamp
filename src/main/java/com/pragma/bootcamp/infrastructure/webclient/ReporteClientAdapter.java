package com.pragma.bootcamp.infrastructure.webclient;

import com.pragma.bootcamp.domain.models.BootcampReporte;
import com.pragma.bootcamp.domain.ports.out.IReporteServicePort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReporteClientAdapter implements IReporteServicePort {

    private static final String SERVICE_CLIENT = "reporte-client";

    private final WebClient.Builder webClientBuilder;

    private WebClient client() {
        return webClientBuilder.baseUrl("http://localhost:8085").build();
    }

    @Override
    @CircuitBreaker(name = SERVICE_CLIENT, fallbackMethod = "generarReporteFallback")
    public Mono<BootcampReporte> generarReporte(BootcampReporte reporte) {
        log.info("ReporteClient: Generando reporte para bootcamp ID: {}", reporte.getBootcampId());
        
        // Crear el request body para el servicio de reportes
        Map<String, Object> request = new HashMap<>();
        request.put("bootcampId", String.valueOf(reporte.getBootcampId()));
        request.put("bootcampNombre", reporte.getBootcampNombre());
        request.put("bootcampDescripcion", reporte.getBootcampDescripcion());
        request.put("fechaLanzamiento", reporte.getFechaLanzamiento() != null ? reporte.getFechaLanzamiento().toString() : null);
        request.put("duracionSemanas", reporte.getDuracionSemanas());
        request.put("cantidadPersonasInscritas", reporte.getCantidadPersonasInscritas());
        request.put("cantidadCapacidades", reporte.getCantidadCapacidades());
        request.put("cantidadTecnologias", reporte.getCantidadTecnologias());

        return client()
                .post()
                .uri("/api/reporte/generar")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus.BAD_REQUEST::equals,
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new IllegalArgumentException(body))))
                .bodyToMono(BootcampReporte.class)
                .doOnSuccess(r -> log.info("ReporteClient: Reporte generado con ID: {}", r.getId()))
                .doOnError(e -> log.error("ReporteClient: Error generando reporte: {}", e.getMessage()));
    }
}
