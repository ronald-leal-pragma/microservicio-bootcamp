package com.pragma.bootcamp.infrastructure.web;

import com.pragma.bootcamp.application.dtos.requests.BootcampRequest;
import com.pragma.bootcamp.application.mappers.BootcampMapper;
import com.pragma.bootcamp.domain.ports.in.IBootcampServicePort;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootcampHandler {

    private final IBootcampServicePort bootcampServicePort;
    private final BootcampMapper bootcampMapper;
    private final Validator validator;

    public Mono<ServerResponse> saveBootcamp(ServerRequest request) {
        return request.bodyToMono(BootcampRequest.class)
                .doOnNext(req -> log.info("Request recibido: {}", req))
                .map(bootcampMapper::toDomain)
                .doOnNext(domain -> log.info("Domain convertido: {}", domain))
                .flatMap(bootcampServicePort::saveBootcamp)
                .doOnNext(saved -> log.info("Bootcamp guardado: {}", saved))
                .flatMap(saved -> ServerResponse.created(URI.create("/api/bootcamp/" + saved.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(bootcampMapper.toResponse(saved)))
                .onErrorResume(DuplicateKeyException.class, e -> {
                    log.warn("Intento de crear bootcamp con nombre duplicado: {}", e.getMessage());
                    return ServerResponse.status(HttpStatus.CONFLICT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                    "error", "BOOTCAMP_YA_EXISTE",
                                    "mensaje", "Ya existe un bootcamp con ese nombre. Por favor, utilice un nombre diferente."
                            ));
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    log.warn("Error de validación: {}", e.getMessage());
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                    "error", "VALIDACION_ERROR",
                                    "mensaje", e.getMessage()
                            ));
                })
                .onErrorResume(e -> {
                    log.error("Error inesperado al guardar bootcamp", e);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                    "error", "ERROR_INTERNO",
                                    "mensaje", "Ocurrió un error al procesar la solicitud. Por favor, intente nuevamente."
                            ));
                });
    }

    public Mono<ServerResponse> getAllBootcamps(ServerRequest request) {
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        String sort = request.queryParam("sort").orElse("nombre");
        String order = request.queryParam("order").orElse("asc");
        
        return bootcampServicePort.getAllBootcamps(page, size, sort, order)
                .collectList()
                .flatMap(bootcamps -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(bootcamps));
    }

    public Mono<ServerResponse> getBootcampById(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));

        return Mono.just(id)
                .doOnSubscribe(s -> log.info("Method: getBootcampById - Input: id={}", id))
                .flatMap(bootcampServicePort::getBootcampById)
                .map(bootcampMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response)
                )
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Method: getBootcampById - Resource not found for id: {}", id);
                    return ServerResponse.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                    "code", "NOT_FOUND",
                                    "message", "404 NOT_FOUND",
                                    "timestamp", LocalDateTime.now().toString()
                            ));
                }))
                .doOnSuccess(res -> log.info("Method: getBootcampById - Output: status={}", res.statusCode()))
                .doOnError(error -> log.error("Method: getBootcampById - Error: {}", error.getMessage()));
    }

    public Mono<ServerResponse> deleteBootcamp(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));
        log.info("Handler: deleteBootcamp - Solicitud de eliminación para bootcamp ID: {}", id);
        
        return bootcampServicePort.deleteBootcamp(id)
                .then(ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of(
                                "mensaje", "Bootcamp eliminado con éxito",
                                "id", id,
                                "timestamp", LocalDateTime.now().toString()
                        )))
                .onErrorResume(e -> {
                    if (e.getMessage().contains("no existe")) {
                        log.warn("Handler: deleteBootcamp - Bootcamp no encontrado: {}", id);
                        return ServerResponse.status(HttpStatus.NOT_FOUND)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of(
                                        "code", "NOT_FOUND",
                                        "message", "404 NOT_FOUND",
                                        "timestamp", LocalDateTime.now().toString()
                                ));
                    }
                    log.error("Handler: deleteBootcamp - Error al eliminar bootcamp {}: {}", id, e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                    "error", "ERROR_ELIMINACION",
                                    "mensaje", "Error al eliminar el bootcamp: " + e.getMessage()
                            ));
                });
    }

    public Mono<ServerResponse> getBootcampCompleto(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));
        log.info("Handler: getBootcampCompleto - Solicitud para bootcamp ID: {}", id);
        
        return bootcampServicePort.getBootcampCompleto(id)
                .flatMap(response -> {
                    log.info("Handler: getBootcampCompleto - Bootcamp '{}' con {} personas inscritas, {} capacidades, {} tecnologías",
                            response.getNombre(),
                            response.getPersonasInscritas().size(),
                            response.getCapacidades().size(),
                            response.getTecnologias().size());
                    
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(e -> {
                    if (e.getMessage().contains("no encontrado")) {
                        log.warn("Handler: getBootcampCompleto - Bootcamp no encontrado: {}", id);
                        return ServerResponse.status(HttpStatus.NOT_FOUND)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of(
                                        "code", "NOT_FOUND",
                                        "message", "Bootcamp no encontrado con ID: " + id,
                                        "timestamp", LocalDateTime.now().toString()
                                ));
                    }
                    log.error("Handler: getBootcampCompleto - Error: {}", e.getMessage());
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                    "error", "ERROR_INTERNO",
                                    "mensaje", "Error al obtener información del bootcamp: " + e.getMessage()
                            ));
                });
    }
}
