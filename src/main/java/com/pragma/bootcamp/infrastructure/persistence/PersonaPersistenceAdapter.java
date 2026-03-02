package com.pragma.bootcamp.infrastructure.persistence;

import com.pragma.bootcamp.domain.models.Persona;
import com.pragma.bootcamp.domain.ports.out.IPersonaPersistencePort;
import com.pragma.bootcamp.infrastructure.entities.PersonaEntity;
import com.pragma.bootcamp.infrastructure.r2dbc.IPersonaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonaPersistenceAdapter implements IPersonaPersistencePort {

    private final IPersonaRepository personaRepository;

    @Override
    public Mono<Persona> savePersona(Persona persona) {
        log.info("Adapter: savePersona - Guardando persona: {}", persona.getNombre());
        PersonaEntity entity = toEntity(persona);
        return personaRepository.save(entity)
                .map(this::toModel)
                .doOnSuccess(saved -> log.info("Adapter: savePersona - Persona guardada con ID: {}", saved.getId()));
    }

    @Override
    public Mono<Persona> findById(Long id) {
        log.info("Adapter: findById - Buscando persona ID: {}", id);
        return personaRepository.findById(id)
                .map(this::toModel);
    }

    @Override
    public Mono<Persona> findByEmail(String email) {
        log.info("Adapter: findByEmail - Buscando persona por email: {}", email);
        return personaRepository.findByEmail(email)
                .map(this::toModel);
    }

    @Override
    public Mono<Persona> findByDocumento(String documento) {
        log.info("Adapter: findByDocumento - Buscando persona por documento: {}", documento);
        return personaRepository.findByDocumento(documento)
                .map(this::toModel);
    }

    private PersonaEntity toEntity(Persona persona) {
        return PersonaEntity.builder()
                .id(persona.getId())
                .nombre(persona.getNombre())
                .apellido(persona.getApellido())
                .email(persona.getEmail())
                .documento(persona.getDocumento())
                .build();
    }

    private Persona toModel(PersonaEntity entity) {
        return Persona.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .apellido(entity.getApellido())
                .email(entity.getEmail())
                .documento(entity.getDocumento())
                .build();
    }
}
