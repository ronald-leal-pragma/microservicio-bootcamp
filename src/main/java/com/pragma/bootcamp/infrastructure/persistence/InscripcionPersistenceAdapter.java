package com.pragma.bootcamp.infrastructure.persistence;

import com.pragma.bootcamp.domain.models.Inscripcion;
import com.pragma.bootcamp.domain.ports.out.IInscripcionPersistencePort;
import com.pragma.bootcamp.infrastructure.entities.InscripcionEntity;
import com.pragma.bootcamp.infrastructure.r2dbc.IInscripcionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class InscripcionPersistenceAdapter implements IInscripcionPersistencePort {

    private final IInscripcionRepository inscripcionRepository;

    @Override
    public Mono<Inscripcion> saveInscripcion(Inscripcion inscripcion) {
        log.info("Adapter: saveInscripcion - Guardando inscripción persona: {}, bootcamp: {}", 
                inscripcion.getPersonaId(), inscripcion.getBootcampId());
        InscripcionEntity entity = toEntity(inscripcion);
        return inscripcionRepository.save(entity)
                .map(this::toModel)
                .doOnSuccess(saved -> log.info("Adapter: saveInscripcion - Inscripción guardada con ID: {}", saved.getId()));
    }

    @Override
    public Mono<Inscripcion> findById(Long id) {
        log.info("Adapter: findById - Buscando inscripción ID: {}", id);
        return inscripcionRepository.findById(id)
                .map(this::toModel);
    }

    @Override
    public Flux<Inscripcion> findByPersonaIdActivas(Long personaId) {
        log.info("Adapter: findByPersonaIdActivas - Buscando inscripciones activas de persona ID: {}", personaId);
        return inscripcionRepository.findByPersonaIdAndEstadoActiva(personaId)
                .map(this::toModel);
    }

    @Override
    public Flux<Inscripcion> findByPersonaId(Long personaId) {
        log.info("Adapter: findByPersonaId - Buscando todas las inscripciones de persona ID: {}", personaId);
        return inscripcionRepository.findByPersonaId(personaId)
                .map(this::toModel);
    }

    @Override
    public Mono<Long> countInscripcionesActivasByPersonaId(Long personaId) {
        log.info("Adapter: countInscripcionesActivasByPersonaId - Contando inscripciones activas de persona ID: {}", personaId);
        return inscripcionRepository.countByPersonaIdAndEstadoActiva(personaId);
    }

    private InscripcionEntity toEntity(Inscripcion inscripcion) {
        return InscripcionEntity.builder()
                .id(inscripcion.getId())
                .personaId(inscripcion.getPersonaId())
                .bootcampId(inscripcion.getBootcampId())
                .fechaInscripcion(inscripcion.getFechaInscripcion())
                .estado(inscripcion.getEstado())
                .build();
    }

    private Inscripcion toModel(InscripcionEntity entity) {
        return Inscripcion.builder()
                .id(entity.getId())
                .personaId(entity.getPersonaId())
                .bootcampId(entity.getBootcampId())
                .fechaInscripcion(entity.getFechaInscripcion())
                .estado(entity.getEstado())
                .build();
    }
}
