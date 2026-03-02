package com.pragma.bootcamp.application.usecase;

import com.pragma.bootcamp.domain.models.Persona;
import com.pragma.bootcamp.domain.ports.in.IPersonaServicePort;
import com.pragma.bootcamp.domain.ports.out.IPersonaPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonaUseCase implements IPersonaServicePort {

    private final IPersonaPersistencePort personaPersistencePort;

    @Override
    public Mono<Persona> createPersona(Persona persona) {
        log.info("UseCase: createPersona - Creando persona con email: {}", persona.getEmail());
        
        // Validar que el email no exista
        return personaPersistencePort.findByEmail(persona.getEmail())
                .flatMap(existente -> {
                    log.warn("UseCase: createPersona - Email ya existe: {}", persona.getEmail());
                    return Mono.<Persona>error(new IllegalArgumentException("El email ya está registrado"));
                })
                .switchIfEmpty(Mono.defer(() -> 
                        personaPersistencePort.findByDocumento(persona.getDocumento())
                                .flatMap(existente -> {
                                    log.warn("UseCase: createPersona - Documento ya existe: {}", persona.getDocumento());
                                    return Mono.<Persona>error(new IllegalArgumentException("El documento ya está registrado"));
                                })
                                .switchIfEmpty(Mono.defer(() -> 
                                        personaPersistencePort.savePersona(persona)
                                                .doOnSuccess(saved -> log.info("UseCase: createPersona - Persona creada con ID: {}", saved.getId()))
                                ))
                ));
    }

    @Override
    public Mono<Persona> getPersonaById(Long id) {
        log.info("UseCase: getPersonaById - Buscando persona ID: {}", id);
        return personaPersistencePort.findById(id)
                .doOnNext(persona -> log.info("UseCase: getPersonaById - Persona encontrada: {}", persona.getNombre()));
    }

    @Override
    public Mono<Persona> getPersonaByEmail(String email) {
        log.info("UseCase: getPersonaByEmail - Buscando persona por email: {}", email);
        return personaPersistencePort.findByEmail(email);
    }
}
