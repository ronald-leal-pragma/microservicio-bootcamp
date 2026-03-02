package com.pragma.bootcamp.domain.ports.in;

import com.pragma.bootcamp.domain.models.Persona;
import reactor.core.publisher.Mono;

public interface IPersonaServicePort {
    Mono<Persona> createPersona(Persona persona);
    Mono<Persona> getPersonaById(Long id);
    Mono<Persona> getPersonaByEmail(String email);
}
