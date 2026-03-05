package com.pragma.bootcamp.infrastructure.r2dbc;

import com.pragma.bootcamp.infrastructure.entities.InscripcionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IInscripcionRepository extends ReactiveCrudRepository<InscripcionEntity, Long> {

    Flux<InscripcionEntity> findByPersonaIdAndEstado(Long personaId, String estado);

    Flux<InscripcionEntity> findByPersonaId(Long personaId);

    Flux<InscripcionEntity> findByBootcampIdAndEstado(Long bootcampId, String estado);

    Mono<Long> countByPersonaIdAndEstado(Long personaId, String estado);

    Mono<Long> countByBootcampIdAndEstado(Long bootcampId, String estado);
}
