package com.pragma.bootcamp.infrastructure.r2dbc;

import com.pragma.bootcamp.infrastructure.entities.PersonaEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface IPersonaRepository extends ReactiveCrudRepository<PersonaEntity, Long> {
    
    @Query("SELECT * FROM persona WHERE email = :email")
    Mono<PersonaEntity> findByEmail(String email);
    
    @Query("SELECT * FROM persona WHERE documento = :documento")
    Mono<PersonaEntity> findByDocumento(String documento);
}
