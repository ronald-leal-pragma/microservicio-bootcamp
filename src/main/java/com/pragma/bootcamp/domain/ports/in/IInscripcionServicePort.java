package com.pragma.bootcamp.domain.ports.in;

import com.pragma.bootcamp.domain.models.Inscripcion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IInscripcionServicePort {
    Mono<Inscripcion> inscribirPersonaEnBootcamp(Long personaId, Long bootcampId);
    Flux<Inscripcion> getInscripcionesByPersonaId(Long personaId);
    Mono<Inscripcion> getInscripcionById(Long id);
}
