package com.pragma.bootcamp.infrastructure.endpoints.web;

import com.pragma.bootcamp.domain.models.Inscripcion;
import com.pragma.bootcamp.domain.ports.in.IInscripcionServicePort;
import com.pragma.bootcamp.application.dtos.responses.BootcampCompleteResponse;
import com.pragma.bootcamp.application.dtos.responses.CapacidadSimpleResponse;
import com.pragma.bootcamp.application.dtos.responses.TecnologiaSimpleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InscripcionHandler {

    private final IInscripcionServicePort inscripcionServicePort;
    private final com.pragma.bootcamp.domain.ports.out.ICapacidadServicePort capacidadServicePort;
    private final Validator validator;

    public Mono<ServerResponse> inscribirPersona(ServerRequest request) {
        return request.bodyToMono(InscripcionRequest.class)
                .flatMap(this::validate)
                .flatMap(inscripcionRequest -> {
                    log.info("Handler: inscribirPersona - Inscribiendo persona {} en bootcamp {}", 
                            inscripcionRequest.getPersonaId(), inscripcionRequest.getBootcampId());
                    
                    return inscripcionServicePort.inscribirPersonaEnBootcamp(
                            inscripcionRequest.getPersonaId(),
                            inscripcionRequest.getBootcampId()
                    );
                })
                .flatMap(inscripcion -> toResponseWithCapacidades(inscripcion)
                    .flatMap(response -> ServerResponse
                            .status(HttpStatus.CREATED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response)))
                .onErrorResume(IllegalArgumentException.class, e -> {
                    log.error("Error de validación al inscribir persona: {}", e.getMessage());
                    return ServerResponse
                            .status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                    "code", "BAD_REQUEST",
                                    "message", e.getMessage(),
                                    "timestamp", LocalDateTime.now().toString()
                            ));
                })
                .onErrorResume(e -> {
                    log.error("Error inesperado al inscribir persona", e);
                    return ServerResponse
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                    "code", "INTERNAL_SERVER_ERROR",
                                    "message", "Error al inscribir persona: " + e.getMessage(),
                                    "timestamp", LocalDateTime.now().toString()
                            ));
                });
    }

    public Mono<ServerResponse> getInscripcionById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        log.info("Handler: getInscripcionById - Buscando inscripción ID: {}", id);
        
        return inscripcionServicePort.getInscripcionById(id)
                .flatMap(inscripcion -> toResponseWithCapacidades(inscripcion)
                    .flatMap(response -> ServerResponse
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response)))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Handler: getInscripcionById - Inscripción ID {} no encontrada", id);
                    return ServerResponse
                            .status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                    "code", "NOT_FOUND",
                                    "message", "404 NOT_FOUND",
                                    "timestamp", LocalDateTime.now().toString()
                            ));
                }));
    }

    public Mono<ServerResponse> getInscripcionesByPersonaId(ServerRequest request) {
        Long personaId = Long.parseLong(request.pathVariable("personaId"));
        log.info("Handler: getInscripcionesByPersonaId - Buscando inscripciones de persona ID: {}", personaId);
        
        return inscripcionServicePort.getInscripcionesByPersonaId(personaId)
                .flatMap(this::toResponseWithCapacidades)
                .collectList()
                .flatMap(inscripciones -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(inscripciones)
                );
    }

    private InscripcionResponse toResponse(Inscripcion inscripcion) {
        InscripcionResponse.InscripcionResponseBuilder builder = InscripcionResponse.builder()
                .id(inscripcion.getId())
                .personaId(inscripcion.getPersonaId())
                .bootcampId(inscripcion.getBootcampId())
                .fechaInscripcion(inscripcion.getFechaInscripcion())
                .estado(inscripcion.getEstado());

        if (inscripcion.getPersona() != null) {
            PersonaResponse personaResponse = PersonaResponse.builder()
                    .id(inscripcion.getPersona().getId())
                    .nombre(inscripcion.getPersona().getNombre())
                    .apellido(inscripcion.getPersona().getApellido())
                    .email(inscripcion.getPersona().getEmail())
                    .documento(inscripcion.getPersona().getDocumento())
                    .build();
            builder.persona(personaResponse);
        }

        if (inscripcion.getBootcamp() != null) {
            BootcampCompleteResponse bootcampResponse = new BootcampCompleteResponse();
            bootcampResponse.setId(inscripcion.getBootcamp().getId());
            bootcampResponse.setNombre(inscripcion.getBootcamp().getNombre());
            bootcampResponse.setDescripcion(inscripcion.getBootcamp().getDescripcion());
            bootcampResponse.setFechaLanzamiento(inscripcion.getBootcamp().getFechaLanzamiento());
            bootcampResponse.setDuracionSemanas(inscripcion.getBootcamp().getDuracionSemanas());
            bootcampResponse.setCapacidades(null);
            
            builder.bootcamp(bootcampResponse);
        }

        return builder.build();
    }

    private Mono<InscripcionResponse> toResponseWithCapacidades(Inscripcion inscripcion) {
        if (inscripcion.getBootcamp() == null || 
            inscripcion.getBootcamp().getCapacidadesIds() == null || 
            inscripcion.getBootcamp().getCapacidadesIds().isEmpty()) {
            return Mono.just(toResponse(inscripcion));
        }

        return capacidadServicePort.getCapacidadesByIds(inscripcion.getBootcamp().getCapacidadesIds())
                .collectList()
                .map(capacidades -> {
                    InscripcionResponse response = toResponse(inscripcion);
                    if (response.getBootcamp() != null) {
                        response.getBootcamp().setCapacidades(capacidades);
                    }
                    return response;
                });
    }

    private Mono<InscripcionRequest> validate(InscripcionRequest inscripcionRequest) {
        Errors errors = new BeanPropertyBindingResult(inscripcionRequest, InscripcionRequest.class.getName());
        validator.validate(inscripcionRequest, errors);
        
        if (errors.hasErrors()) {
            String errorMsg = errors.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Error de validación");
            return Mono.error(new IllegalArgumentException(errorMsg));
        }
        
        return Mono.just(inscripcionRequest);
    }
}
