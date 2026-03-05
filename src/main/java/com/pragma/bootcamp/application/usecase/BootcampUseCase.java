package com.pragma.bootcamp.application.usecase;

import com.pragma.bootcamp.application.dtos.responses.*;
import com.pragma.bootcamp.domain.models.Bootcamp;
import com.pragma.bootcamp.domain.ports.in.IBootcampServicePort;
import com.pragma.bootcamp.domain.ports.out.IBootcampPersistencePort;
import com.pragma.bootcamp.domain.ports.out.ICapacidadServicePort;
import com.pragma.bootcamp.infrastructure.r2dbc.IInscripcionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.pragma.bootcamp.domain.ports.out.IReporteServicePort;
import com.pragma.bootcamp.domain.models.BootcampReporte;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BootcampUseCase implements IBootcampServicePort {

    private final IBootcampPersistencePort bootcampPersistencePort;
    private final ICapacidadServicePort capacidadServicePort;
    private final IInscripcionRepository inscripcionRepository;
    private final com.pragma.bootcamp.domain.ports.out.IPersonaServicePort personaServicePort;
    private final IReporteServicePort reporteServicePort;

    @Override
    public Mono<Bootcamp> saveBootcamp(Bootcamp bootcamp) {
        log.info("UseCase - Recibido bootcamp: {}", bootcamp);
        if (bootcamp.getCapacidadesIds() == null || bootcamp.getCapacidadesIds().isEmpty() || bootcamp.getCapacidadesIds().size() > 4) {
            return Mono.error(new IllegalArgumentException("Un bootcamp debe tener entre 1 y 4 capacidades asociadas"));
        }

        return capacidadServicePort.validateCapacidadesBatch(bootcamp.getCapacidadesIds())
                .then(bootcampPersistencePort.save(bootcamp))
                .doOnSuccess(this::sendReportAsync)
                .doOnNext(saved -> log.info("UseCase - Bootcamp guardado: {}", saved));
    }

    private void sendReportAsync(Bootcamp bootcamp) {
        Mono.just(bootcamp)
                .flatMap(b -> {
                    if (b.getCapacidadesIds() == null || b.getCapacidadesIds().isEmpty()) {
                        return Mono.just(0);
                    }
                    return capacidadServicePort.getCapacidadesByIds(b.getCapacidadesIds())
                            .flatMap(cap -> Flux.fromIterable(cap.getTecnologias() != null ? cap.getTecnologias() : java.util.Collections.emptyList()))
                            .map(TecnologiaSimpleResponse::getId)
                            .collect(Collectors.toSet())
                            .map(Set::size);
                })
                .flatMap(techCount -> {
                    BootcampReporte reporte = BootcampReporte.builder()
                            .bootcampId(bootcamp.getId())
                            .bootcampNombre(bootcamp.getNombre())
                            .bootcampDescripcion(bootcamp.getDescripcion())
                            .fechaLanzamiento(bootcamp.getFechaLanzamiento())
                            .duracionSemanas(bootcamp.getDuracionSemanas())
                            .cantidadCapacidades(bootcamp.getCapacidadesIds() != null ? bootcamp.getCapacidadesIds().size() : 0)
                            .cantidadTecnologias(techCount)
                            .cantidadPersonasInscritas(0)
                            .fechaRegistroReporte(LocalDateTime.now())
                            .build();
                    return reporteServicePort.generarReporte(reporte);
                })
                .subscribe(
                        success -> log.info("UseCase: Reporte de bootcamp generado exitosamente en background"),
                        error -> log.error("UseCase: Error al generar reporte de bootcamp en background: {}", error.getMessage())
                );
    }

    @Override
    public Flux<BootcampCompleteResponse> getAllBootcamps(int page, int size, String sort, String order) {
        if ("nombre".equalsIgnoreCase(sort)) {
            // La BD ya aplica el paginado y orden por nombre
            return bootcampPersistencePort.findAll(page, size, sort, order)
                    .flatMap(this::enrichBootcamp);
        }

        // Para cantCapacidades: traer todo, enriquecer, ordenar en memoria y paginar
        Comparator<BootcampCompleteResponse> comparator = Comparator.comparing(
                r -> r.getCapacidades().size());
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        final Comparator<BootcampCompleteResponse> finalComparator = comparator;

        return bootcampPersistencePort.findAll(0, Integer.MAX_VALUE, sort, order)
                .flatMap(this::enrichBootcamp)
                .collectList()
                .flatMapMany(all -> Flux.fromIterable(
                        all.stream()
                                .sorted(finalComparator)
                                .skip((long) page * size)
                                .limit(size)
                                .collect(Collectors.toList())));
    }

    private Mono<BootcampCompleteResponse> enrichBootcamp(Bootcamp bootcamp) {
        if (bootcamp.getCapacidadesIds() == null || bootcamp.getCapacidadesIds().isEmpty()) {
            BootcampCompleteResponse response = new BootcampCompleteResponse();
            response.setId(bootcamp.getId());
            response.setNombre(bootcamp.getNombre());
            response.setDescripcion(bootcamp.getDescripcion());
            response.setFechaLanzamiento(bootcamp.getFechaLanzamiento());
            response.setDuracionSemanas(bootcamp.getDuracionSemanas());
            response.setCapacidades(java.util.Collections.emptyList());
            return Mono.just(response);
        }
        return capacidadServicePort.getCapacidadesByIds(bootcamp.getCapacidadesIds())
                .collectList()
                .map(capacidades -> {
                    BootcampCompleteResponse response = new BootcampCompleteResponse();
                    response.setId(bootcamp.getId());
                    response.setNombre(bootcamp.getNombre());
                    response.setDescripcion(bootcamp.getDescripcion());
                    response.setFechaLanzamiento(bootcamp.getFechaLanzamiento());
                    response.setDuracionSemanas(bootcamp.getDuracionSemanas());
                    response.setCapacidades(capacidades);
                    return response;
                });
    }

    @Override
    public Mono<Bootcamp> getBootcampById(Long id) {
        return bootcampPersistencePort.getBootcampById(id);
    }

    @Override
    @Transactional
    public Mono<Void> deleteBootcamp(Long id) {
        log.info("UseCase: deleteBootcamp - Iniciando eliminación de bootcamp con ID: {}", id);
        
        return bootcampPersistencePort.getBootcampById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("El bootcamp con ID " + id + " no existe")))
                .flatMap(bootcamp -> {
                    Set<Long> capacidadesIds = bootcamp.getCapacidadesIds() != null ? 
                            new HashSet<>(bootcamp.getCapacidadesIds()) : new HashSet<>();
                    
                    log.info("UseCase: deleteBootcamp - Bootcamp encontrado: {}, Capacidades asociadas: {}", 
                            bootcamp.getNombre(), capacidadesIds);
                    
                    // Eliminar el bootcamp
                    return bootcampPersistencePort.deleteBootcamp(id)
                            .then(Mono.just(capacidadesIds));
                })
                .flatMap(capacidadesIds -> {
                    if (capacidadesIds.isEmpty()) {
                        log.info("UseCase: deleteBootcamp - No hay capacidades asociadas para verificar");
                        return Mono.empty();
                    }
                    
                    // Obtener todos los bootcamps restantes
                    return bootcampPersistencePort.findAllBootcamps()
                            .collectList()
                            .flatMap(bootcamps -> {
                                // Recolectar todas las capacidades referenciadas por otros bootcamps
                                Set<Long> capacidadesReferenciadas = bootcamps.stream()
                                        .flatMap(b -> b.getCapacidadesIds() != null ? 
                                                b.getCapacidadesIds().stream() : Set.<Long>of().stream())
                                        .collect(Collectors.toSet());
                                
                                // Identificar capacidades huérfanas (no referenciadas)
                                Set<Long> capacidadesHuerfanas = capacidadesIds.stream()
                                        .filter(capId -> !capacidadesReferenciadas.contains(capId))
                                        .collect(Collectors.toSet());
                                
                                log.info("UseCase: deleteBootcamp - Capacidades a eliminar (huérfanas): {}", capacidadesHuerfanas);
                                
                                // Eliminar capacidades huérfanas
                                return Flux.fromIterable(capacidadesHuerfanas)
                                        .flatMap(capId -> {
                                            log.info("UseCase: deleteBootcamp - Eliminando capacidad huérfana con ID: {}", capId);
                                            return capacidadServicePort.deleteCapacidad(capId)
                                                    .doOnError(e -> log.error("UseCase: deleteBootcamp - Fallo al eliminar capacidad {}. Se hará rollback del bootcamp. Error: {}", capId, e.getMessage()));
                                        })
                                        .then();
                            });
                })
                .then()
                .doOnSuccess(v -> log.info("UseCase: deleteBootcamp - Bootcamp eliminado exitosamente"))
                .doOnError(error -> log.error("UseCase: deleteBootcamp - Error: {}", error.getMessage()));
    }

    @Override
    public Mono<BootcampDetalleCompletoResponse> getBootcampCompleto(Long id) {
        log.info("UseCase - Obteniendo detalle completo del bootcamp ID: {}", id);
        
        return bootcampPersistencePort.getBootcampById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Bootcamp no encontrado con ID: " + id)))
                .flatMap(bootcamp -> {
                    log.info("UseCase - Bootcamp encontrado: {}", bootcamp.getNombre());
                    
                    // Obtener personas inscritas activas
                    Mono<List<PersonaInscritaDTO>> personasInscritasMono = inscripcionRepository
                            .findByBootcampIdAndEstado(id, "ACTIVA")
                            .flatMap(inscripcionEntity -> 
                                personaServicePort.findById(inscripcionEntity.getPersonaId())
                                    .map(persona -> PersonaInscritaDTO.builder()
                                            .nombre(persona.getNombre())
                                            .apellido(persona.getApellido())
                                            .email(persona.getEmail())
                                            .build())
                            )
                            .collectList();
                    
                    // Obtener capacidades con sus tecnologías
                    Mono<List<CapacidadCompleteResponse>> capacidadesMono = 
                            capacidadServicePort.getCapacidadesByIds(bootcamp.getCapacidadesIds())
                                    .map(capacidadSimple -> CapacidadCompleteResponse.builder()
                                            .id(capacidadSimple.getId())
                                            .nombre(capacidadSimple.getNombre())
                                            .descripcion(null) // CapacidadSimpleResponse no tiene descripción
                                            .tecnologias(capacidadSimple.getTecnologias() != null ?
                                                    capacidadSimple.getTecnologias().stream()
                                                            .map(tecSimple -> new TecnologiaResponse(
                                                                    tecSimple.getId(),
                                                                    tecSimple.getNombre(),
                                                                    null)) // TecnologiaSimpleResponse no tiene descripción
                                                            .collect(Collectors.toList()) :
                                                    List.of())
                                            .build())
                                    .collectList();
                    
                    // Combinar todos los datos
                    return Mono.zip(personasInscritasMono, capacidadesMono)
                            .map(tuple -> {
                                List<PersonaInscritaDTO> personasInscritas = tuple.getT1();
                                List<CapacidadCompleteResponse> capacidades = tuple.getT2();
                                
                                // Extraer todas las tecnologías únicas de las capacidades
                                Set<TecnologiaResponse> tecnologiasUnicas = capacidades.stream()
                                        .flatMap(cap -> cap.getTecnologias() != null ? 
                                                cap.getTecnologias().stream() : 
                                                Set.<TecnologiaResponse>of().stream())
                                        .collect(Collectors.toSet());
                                
                                log.info("UseCase - Bootcamp {}: {} personas inscritas, {} capacidades, {} tecnologías",
                                        bootcamp.getNombre(), 
                                        personasInscritas.size(), 
                                        capacidades.size(),
                                        tecnologiasUnicas.size());
                                
                                return BootcampDetalleCompletoResponse.builder()
                                        .id(bootcamp.getId())
                                        .nombre(bootcamp.getNombre())
                                        .descripcion(bootcamp.getDescripcion())
                                        .fechaLanzamiento(bootcamp.getFechaLanzamiento())
                                        .duracionSemanas(bootcamp.getDuracionSemanas())
                                        .personasInscritas(personasInscritas)
                                        .capacidades(capacidades)
                                        .tecnologias(new java.util.ArrayList<>(tecnologiasUnicas))
                                        .build();
                            });
                });
    }

    @Override
    public Mono<Long> countBootcamps() {
        return bootcampPersistencePort.count();
    }
}
