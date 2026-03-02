package com.pragma.bootcamp.infrastructure.endpoints.web;

import com.pragma.bootcamp.domain.models.Persona;
import com.pragma.bootcamp.domain.ports.in.IPersonaServicePort;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonaHandler {

    private final IPersonaServicePort personaServicePort;
    private final Validator validator;

    public Mono<ServerResponse> createPersona(ServerRequest request) {
        return request.bodyToMono(PersonaRequest.class)
                .flatMap(this::validate)
                .flatMap(personaRequest -> {
                    Persona persona = Persona.builder()
                            .nombre(personaRequest.getNombre())
                            .apellido(personaRequest.getApellido())
                            .email(personaRequest.getEmail())
                            .documento(personaRequest.getDocumento())
                            .build();
                    
                    return personaServicePort.createPersona(persona);
                })
                .flatMap(persona -> {
                    PersonaResponse response = PersonaResponse.builder()
                            .id(persona.getId())
                            .nombre(persona.getNombre())
                            .apellido(persona.getApellido())
                            .email(persona.getEmail())
                            .documento(persona.getDocumento())
                            .build();
                    
                    return ServerResponse
                            .status(HttpStatus.CREATED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    log.error("Error de validación al crear persona: {}", e.getMessage());
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
                    log.error("Error inesperado al crear persona", e);
                    return ServerResponse
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                    "code", "INTERNAL_SERVER_ERROR",
                                    "message", "Error al crear persona: " + e.getMessage(),
                                    "timestamp", LocalDateTime.now().toString()
                            ));
                });
    }

    public Mono<ServerResponse> getPersonaById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        log.info("Handler: getPersonaById - Buscando persona ID: {}", id);
        
        return personaServicePort.getPersonaById(id)
                .flatMap(persona -> {
                    PersonaResponse response = PersonaResponse.builder()
                            .id(persona.getId())
                            .nombre(persona.getNombre())
                            .apellido(persona.getApellido())
                            .email(persona.getEmail())
                            .documento(persona.getDocumento())
                            .build();
                    
                    return ServerResponse
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Handler: getPersonaById - Persona ID {} no encontrada", id);
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

    private Mono<PersonaRequest> validate(PersonaRequest personaRequest) {
        Errors errors = new BeanPropertyBindingResult(personaRequest, PersonaRequest.class.getName());
        validator.validate(personaRequest, errors);
        
        if (errors.hasErrors()) {
            String errorMsg = errors.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Error de validación");
            return Mono.error(new IllegalArgumentException(errorMsg));
        }
        
        return Mono.just(personaRequest);
    }
}
