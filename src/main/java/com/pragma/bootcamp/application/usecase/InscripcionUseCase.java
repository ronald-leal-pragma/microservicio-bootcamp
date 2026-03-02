package com.pragma.bootcamp.application.usecase;

import com.pragma.bootcamp.domain.models.Bootcamp;
import com.pragma.bootcamp.domain.models.Inscripcion;
import com.pragma.bootcamp.domain.ports.in.IBootcampServicePort;
import com.pragma.bootcamp.domain.ports.in.IInscripcionServicePort;
import com.pragma.bootcamp.domain.ports.out.IInscripcionPersistencePort;
import com.pragma.bootcamp.domain.ports.out.IPersonaPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InscripcionUseCase implements IInscripcionServicePort {

    private final IInscripcionPersistencePort inscripcionPersistencePort;
    private final IPersonaPersistencePort personaPersistencePort;
    private final IBootcampServicePort bootcampServicePort;

    @Override
    @Transactional
    public Mono<Inscripcion> inscribirPersonaEnBootcamp(Long personaId, Long bootcampId) {
        log.info("UseCase: inscribirPersonaEnBootcamp - Persona ID: {}, Bootcamp ID: {}", personaId, bootcampId);

        // 1. Validar que la persona existe
        return personaPersistencePort.findById(personaId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("La persona con ID " + personaId + " no existe")))
                .flatMap(persona -> {
                    log.info("UseCase: inscribirPersonaEnBootcamp - Persona encontrada: {}", persona.getNombre());
                    
                    // 2. Validar que el bootcamp existe
                    return bootcampServicePort.getBootcampById(bootcampId)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("El bootcamp con ID " + bootcampId + " no existe")))
                            .flatMap(bootcampNuevo -> {
                                log.info("UseCase: inscribirPersonaEnBootcamp - Bootcamp encontrado: {}", bootcampNuevo.getNombre());
                                
                                // 3. Validar máximo 5 inscripciones activas
                                return inscripcionPersistencePort.countInscripcionesActivasByPersonaId(personaId)
                                        .flatMap(count -> {
                                            if (count >= 5) {
                                                log.warn("UseCase: inscribirPersonaEnBootcamp - Máximo de 5 inscripciones alcanzado");
                                                return Mono.error(new IllegalArgumentException(
                                                        "La persona ya tiene el máximo de 5 inscripciones activas"));
                                            }
                                            
                                            // 4. Obtener inscripciones activas y validar solapamiento de fechas
                                            return inscripcionPersistencePort.findByPersonaIdActivas(personaId)
                                                    .flatMap(inscripcion -> bootcampServicePort.getBootcampById(inscripcion.getBootcampId()))
                                                    .collectList()
                                                    .flatMap(bootcampsActivos -> {
                                                        // Validar que no haya solapamiento de fechas
                                                        if (existeSolapamientoFechas(bootcampNuevo, bootcampsActivos)) {
                                                            log.warn("UseCase: inscribirPersonaEnBootcamp - Solapamiento de fechas detectado");
                                                            return Mono.error(new IllegalArgumentException(
                                                                    "Ya existe una inscripción activa en un bootcamp con fechas que se solapan"));
                                                        }
                                                        
                                                        // 5. Crear la inscripción
                                                        Inscripcion nuevaInscripcion = Inscripcion.builder()
                                                                .personaId(personaId)
                                                                .bootcampId(bootcampId)
                                                                .fechaInscripcion(LocalDate.now())
                                                                .estado("ACTIVA")
                                                                .build();
                                                        
                                                        return inscripcionPersistencePort.saveInscripcion(nuevaInscripcion)
                                                                .doOnSuccess(saved -> log.info(
                                                                        "UseCase: inscribirPersonaEnBootcamp - Inscripción creada con ID: {}", 
                                                                        saved.getId()));
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
        return personaPersistencePort.findById(inscripcion.getPersonaId())
                .flatMap(persona -> {
                    inscripcion.setPersona(persona);
                    return bootcampServicePort.getBootcampById(inscripcion.getBootcampId())
                            .map(bootcamp -> {
                                inscripcion.setBootcamp(bootcamp);
                                return inscripcion;
                            });
                });
    }

    /**
     * Valida si existe solapamiento de fechas entre el bootcamp nuevo y los bootcamps activos.
     * Solapamiento ocurre si:
     * - El inicio del nuevo bootcamp está dentro del rango de un bootcamp activo
     * - El fin del nuevo bootcamp está dentro del rango de un bootcamp activo
     * - El nuevo bootcamp contiene completamente a un bootcamp activo
     */
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
}
