package com.pragma.bootcamp.infrastructure.mongodb;

import com.pragma.bootcamp.infrastructure.entities.BootcampReporteMongoEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface IBootcampReporteMongoRepository
        extends ReactiveMongoRepository<BootcampReporteMongoEntity, String> {

    Mono<BootcampReporteMongoEntity> findFirstByBootcampIdOrderByFechaGeneracionDesc(String bootcampId);
}
