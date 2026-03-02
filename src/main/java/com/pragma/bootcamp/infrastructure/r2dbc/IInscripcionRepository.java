package com.pragma.bootcamp.infrastructure.r2dbc;

import com.pragma.bootcamp.infrastructure.entities.InscripcionEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IInscripcionRepository extends ReactiveCrudRepository<InscripcionEntity, Long> {
    
    @Query("SELECT * FROM inscripcion WHERE persona_id = :personaId AND estado = 'ACTIVA'")
    Flux<InscripcionEntity> findByPersonaIdAndEstadoActiva(Long personaId);
    
    @Query("SELECT * FROM inscripcion WHERE persona_id = :personaId")
    Flux<InscripcionEntity> findByPersonaId(Long personaId);
    
    @Query("SELECT * FROM inscripcion WHERE bootcamp_id = :bootcampId AND estado = 'ACTIVA'")
    Flux<InscripcionEntity> findByBootcampIdAndEstadoActiva(Long bootcampId);
    
    @Query("SELECT COUNT(*) FROM inscripcion WHERE persona_id = :personaId AND estado = 'ACTIVA'")
    Mono<Long> countByPersonaIdAndEstadoActiva(Long personaId);
    
    @Query("SELECT COUNT(*) FROM inscripcion WHERE bootcamp_id = :bootcampId AND estado = 'ACTIVA'")
    Mono<Long> countByBootcampIdAndEstadoActiva(Long bootcampId);
}
