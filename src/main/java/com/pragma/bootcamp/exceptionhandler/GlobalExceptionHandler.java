package com.pragma.bootcamp.exceptionhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.bootcamp.infrastructure.entities.ErrorResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(-2) // Prioridad alta para capturar excepciones antes que el manejador por defecto
public class GlobalExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        log.error("Error detectado en ruta [{}]: {}", exchange.getRequest().getPath(), ex.getMessage());

        if (ex instanceof WebExchangeBindException webEx) {
            return handleValidationException(webEx, exchange);
        }

        if (ex instanceof ResponseStatusException resEx) {
            return writeResponse(exchange, HttpStatus.valueOf(resEx.getStatusCode().value()), "HTTP_ERROR", resEx.getReason());
        }

        if (ex instanceof IllegalArgumentException illegalEx) {
            String msg = illegalEx.getMessage() != null ? illegalEx.getMessage().toLowerCase() : "";
            if (msg.contains("ya existe")) {
                return writeResponse(exchange, HttpStatus.CONFLICT, "ENTITY_ALREADY_EXISTS", illegalEx.getMessage());
            } else if (msg.contains("no existe") || msg.contains("not found")) {
                return writeResponse(exchange, HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", illegalEx.getMessage());
            }
            return writeResponse(exchange, HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", illegalEx.getMessage());
        }

        // Caso por defecto
        return handleGenericException(exchange, ex);
    }

    private Mono<Void> handleValidationException(WebExchangeBindException ex, ServerWebExchange exchange) {
        String details = ex.getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return writeResponse(exchange, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", details);
    }

    private Mono<Void> handleGenericException(ServerWebExchange exchange, Throwable ex) {
        log.error("Exception no controlada: ", ex);
        return writeResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "Ocurrió un error inesperado en el sistema.");
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, HttpStatus status, String code, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        return exchange.getResponse().writeWith(Mono.fromCallable(() -> {
            try {
                byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
                return exchange.getResponse().bufferFactory().wrap(bytes);
            } catch (Exception e) {
                log.error("Error serializando ErrorResponse", e);
                return exchange.getResponse().bufferFactory().wrap("{}".getBytes());
            }
        }));
    }
}