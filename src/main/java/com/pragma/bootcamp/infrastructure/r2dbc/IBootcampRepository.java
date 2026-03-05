package com.pragma.bootcamp.infrastructure.r2dbc;

import com.pragma.bootcamp.infrastructure.entities.BootcampEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface IBootcampRepository extends R2dbcRepository<BootcampEntity, Long> {
    Flux<BootcampEntity> findAllBy(PageRequest pageRequest);
}
