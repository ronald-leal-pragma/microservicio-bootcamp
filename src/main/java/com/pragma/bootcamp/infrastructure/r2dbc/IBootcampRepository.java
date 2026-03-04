package com.pragma.bootcamp.infrastructure.r2dbc;

import com.pragma.bootcamp.infrastructure.entities.BootcampEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBootcampRepository extends R2dbcRepository<BootcampEntity, Long> {
    // custom queries can be added when needed
}
