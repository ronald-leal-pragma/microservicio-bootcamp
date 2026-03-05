package com.pragma.bootcamp.infrastructure.r2dbc;

import com.pragma.bootcamp.infrastructure.entities.InscripcionEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IInscripcionRepository extends ReactiveCrudRepository<InscripcionEntity, Long> {

    @Query("SELECT * FROM inscripcion WHERE persona_id = :personaId AND estado = 'ACTIVA'")
    Flux<InscripcionEntity> findByPersonaIdAndEstadoActiva(Long personaId);


    Flux<InscripcionEntity> findByPersonaId(Long personaId);

    Flux<InscripcionEntity> findByBootcampIdAndEstadoActiva(Long bootcampId,String estado);

    Mono<Long> countByPersonaIdAndEstadoActiva(Long personaId,String estado);

    Mono<Long> countByBootcampIdAndEstado(Long bootcampId, String estado);
}
