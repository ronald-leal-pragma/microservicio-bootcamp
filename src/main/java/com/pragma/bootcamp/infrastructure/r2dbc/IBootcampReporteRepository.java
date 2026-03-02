package com.pragma.bootcamp.infrastructure.r2dbc;

import com.pragma.bootcamp.infrastructure.entities.BootcampReporteEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface IBootcampReporteRepository extends ReactiveCrudRepository<BootcampReporteEntity, Long> {
}
