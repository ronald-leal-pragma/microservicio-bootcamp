package com.pragma.bootcamp.infrastructure.persistence;

import com.pragma.bootcamp.domain.models.Bootcamp;
import com.pragma.bootcamp.domain.ports.out.IBootcampPersistencePort;
import com.pragma.bootcamp.infrastructure.r2dbc.IBootcampRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootcampPersistenceAdapter implements IBootcampPersistencePort {

    private static final String SERVICE_OPERATION_DB = "bootcamp-db";

    private final IBootcampRepository bootcampRepository;
    private final BootcampEntityMapper bootcampEntityMapper;

    @Override
    public Mono<Bootcamp> save(Bootcamp bootcamp) {
        log.info("PersistenceAdapter - Bootcamp recibido para guardar: {}", bootcamp);
        var entity = bootcampEntityMapper.toEntity(bootcamp);
        log.info("PersistenceAdapter - Entity convertida: {}", entity);
        return bootcampRepository.save(entity)
                .doOnNext(saved -> log.info("PersistenceAdapter - Entity guardada: {}", saved))
                .map(bootcampEntityMapper::toDomain)
                .doOnNext(domain -> log.info("PersistenceAdapter - Domain retornado: {}", domain));
    }

    @Override
    public Flux<Bootcamp> findAll(int page, int size) {
        return bootcampRepository.findAll()
                .map(bootcampEntityMapper::toDomain);
    }

    @Override
    @CircuitBreaker(name = SERVICE_OPERATION_DB)
    public Mono<Bootcamp> getBootcampById(Long id) {
        log.debug("Persistence: Buscando bootcamp por ID: {}", id);
        return bootcampRepository.findById(id)
                .map(bootcampEntityMapper::toDomain)
                .doOnError(e -> log.error("Persistence: Error al buscar bootcamp ID {}: {}", id, e.getMessage()));
    }

    @Override
    public Mono<Void> deleteBootcamp(Long id) {
        log.info("PersistenceAdapter - Eliminando bootcamp con ID: {}", id);
        return bootcampRepository.deleteById(id)
                .doOnSuccess(v -> log.info("PersistenceAdapter - Bootcamp con ID {} eliminado exitosamente", id))
                .doOnError(e -> log.error("PersistenceAdapter - Error al eliminar bootcamp ID {}: {}", id, e.getMessage()));
    }

    @Override
    public Flux<Bootcamp> findAllBootcamps() {
        return bootcampRepository.findAll()
                .map(bootcampEntityMapper::toDomain);
    }
}
