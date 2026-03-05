package com.pragma.bootcamp.application.usecase;

import com.pragma.bootcamp.application.dtos.responses.TecnologiaSimpleResponse;
import com.pragma.bootcamp.domain.models.Bootcamp;
import com.pragma.bootcamp.domain.models.Inscripcion;
import com.pragma.bootcamp.domain.ports.in.IBootcampServicePort;
import com.pragma.bootcamp.domain.ports.in.IInscripcionServicePort;
import com.pragma.bootcamp.domain.ports.out.ICapacidadServicePort;
import com.pragma.bootcamp.domain.ports.out.IInscripcionPersistencePort;
import com.pragma.bootcamp.domain.ports.out.IReporteServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class InscripcionUseCase implements IInscripcionServicePort {

    private final IInscripcionPersistencePort inscripcionPersistencePort;
    private final com.pragma.bootcamp.domain.ports.out.IPersonaServicePort personaServicePort;
    private final IBootcampServicePort bootcampServicePort;
    private final IReporteServicePort reporteServicePort;
    private final ICapacidadServicePort capacidadServicePort;

    @Override
    public Mono<Inscripcion> inscribirPersonaEnBootcamp(Long personaId, Long bootcampId) {
        log.info("UseCase: inscribirPersonaEnBootcamp - Persona ID: {}, Bootcamp ID: {}", personaId, bootcampId);

        return personaServicePort.findById(personaId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("La persona con ID " + personaId + " no existe")))
                .flatMap(persona -> {
                    log.info("UseCase: inscribirPersonaEnBootcamp - Persona encontrada: {}", persona.getNombre());
                    
                    return bootcampServicePort.getBootcampById(bootcampId)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("El bootcamp con ID " + bootcampId + " no existe")))
                            .flatMap(bootcampNuevo -> {
                                log.info("UseCase: inscribirPersonaEnBootcamp - Bootcamp encontrado: {}", bootcampNuevo.getNombre());
                                
                                return inscripcionPersistencePort.countInscripcionesActivasByPersonaId(personaId)
                                        .flatMap(count -> {
                                            if (count >= 5) {
                                                log.warn("UseCase: inscribirPersonaEnBootcamp - Máximo de 5 inscripciones alcanzado");
                                                return Mono.error(new IllegalArgumentException(
                                                        "La persona ya tiene el máximo de 5 inscripciones activas"));
                                            }
                                            
                                            return inscripcionPersistencePort.findByPersonaIdActivas(personaId)
                                                    .flatMap(inscripcion -> bootcampServicePort.getBootcampById(inscripcion.getBootcampId()))
                                                    .collectList()
                                                    .flatMap(bootcampsActivos -> {
                                                        if (existeSolapamientoFechas(bootcampNuevo, bootcampsActivos)) {
                                                            log.warn("UseCase: inscribirPersonaEnBootcamp - Solapamiento de fechas detectado");
                                                            return Mono.error(new IllegalArgumentException(
                                                                    "Ya existe una inscripción activa en un bootcamp con fechas que se solapan"));
                                                        }
                                                        
                                                        Inscripcion nuevaInscripcion = Inscripcion.builder()
                                                                .personaId(personaId)
                                                                .bootcampId(bootcampId)
                                                                .fechaInscripcion(LocalDate.now())
                                                                .estado("ACTIVA")
                                                                .build();
                                                        
                                                        return inscripcionPersistencePort.saveInscripcion(nuevaInscripcion)
                                                                .doOnSuccess(saved -> {
                                                                    log.info(
                                                                            "UseCase: inscribirPersonaEnBootcamp - Inscripción creada con ID: {}",
                                                                            saved.getId());
                                                                    sendReporteUpdateAsync(bootcampNuevo);
                                                                })
                                                                .flatMap(this::enrichInscripcion);
                                                    });
                                        });
                            });
                });
    }

    @Override
    public Flux<Inscripcion> getInscripcionesByPersonaId(Long personaId) {
        log.info("UseCase: getInscripcionesByPersonaId - Persona ID: {}", personaId);
        return inscripcionPersistencePort.findByPersonaId(personaId)
                .flatMap(this::enrichInscripcion);
    }

    @Override
    public Mono<Inscripcion> getInscripcionById(Long id) {
        log.info("UseCase: getInscripcionById - Inscripcion ID: {}", id);
        return inscripcionPersistencePort.findById(id)
                .flatMap(this::enrichInscripcion);
    }

    private Mono<Inscripcion> enrichInscripcion(Inscripcion inscripcion) {
        return personaServicePort.findById(inscripcion.getPersonaId())
                .flatMap(persona -> {
                    inscripcion.setPersona(persona);
                    return bootcampServicePort.getBootcampById(inscripcion.getBootcampId())
                            .map(bootcamp -> {
                                inscripcion.setBootcamp(bootcamp);
                                return inscripcion;
                            });
                });
    }

    private boolean existeSolapamientoFechas(Bootcamp bootcampNuevo, List<Bootcamp> bootcampsActivos) {
        LocalDate inicioNuevo = bootcampNuevo.getFechaLanzamiento();
        LocalDate finNuevo = inicioNuevo.plusWeeks(bootcampNuevo.getDuracionSemanas());

        log.info("UseCase: existeSolapamientoFechas - Validando bootcamp: {} ({} a {})",
                bootcampNuevo.getNombre(), inicioNuevo, finNuevo);

        for (Bootcamp bootcampActivo : bootcampsActivos) {
            LocalDate inicioActivo = bootcampActivo.getFechaLanzamiento();
            LocalDate finActivo = inicioActivo.plusWeeks(bootcampActivo.getDuracionSemanas());

            log.info("UseCase: existeSolapamientoFechas - Comparando con: {} ({} a {})",
                    bootcampActivo.getNombre(), inicioActivo, finActivo);

            // Verificar solapamiento
            boolean solapamiento = !(finNuevo.isBefore(inicioActivo) || finNuevo.isEqual(inicioActivo) ||
                                    inicioNuevo.isAfter(finActivo) || inicioNuevo.isEqual(finActivo));

            if (solapamiento) {
                log.warn("UseCase: existeSolapamientoFechas - Solapamiento detectado con bootcamp: {}",
                        bootcampActivo.getNombre());
                return true;
            }
        }

        return false;
    }

    private void sendReporteUpdateAsync(Bootcamp bootcamp) {
        Set<Long> capIds = bootcamp.getCapacidadesIds() != null ? bootcamp.getCapacidadesIds() : new HashSet<>();
        int cantCapacidades = capIds.size();

        Mono<Long> countPersonasMono = inscripcionPersistencePort
                .countInscripcionesActivasByBootcampId(bootcamp.getId());

        Mono<Integer> countTecnologiasMono = capIds.isEmpty()
                ? Mono.just(0)
                : capacidadServicePort.getCapacidadesByIds(capIds)
                        .flatMapIterable(cap -> cap.getTecnologias() != null ? cap.getTecnologias() : List.of())
                        .map(TecnologiaSimpleResponse::getId)
                        .collect(java.util.stream.Collectors.toSet())
                        .map(Set::size)
                        .onErrorReturn(0);


        Mono.zip(countPersonasMono, countTecnologiasMono)
                .flatMap(tuple -> reporteServicePort.actualizarReporte(
                        bootcamp.getId(),
                        tuple.getT1().intValue(),
                        cantCapacidades,
                        tuple.getT2()))
                .onErrorResume(e -> {
                    log.warn("sendReporteUpdateAsync: Error actualizando reporte del bootcamp {}: {}",
                            bootcamp.getId(), e.getMessage());
                    return Mono.empty();
                })
                .subscribe();
    }}