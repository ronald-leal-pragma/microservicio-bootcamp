package com.pragma.bootcamp.domain.ports.in;

import com.pragma.bootcamp.application.dtos.responses.BootcampCompleteResponse;
import com.pragma.bootcamp.application.dtos.responses.BootcampDetalleCompletoResponse;
import com.pragma.bootcamp.domain.models.Bootcamp;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IBootcampServicePort {
    Mono<Bootcamp> saveBootcamp(Bootcamp bootcamp);
    Flux<BootcampCompleteResponse> getAllBootcamps(int page, int size, String sort, String order);
    Mono<Bootcamp> getBootcampById(Long id);
    Mono<Void> deleteBootcamp(Long id);
    Mono<BootcampDetalleCompletoResponse> getBootcampCompleto(Long id);
}
