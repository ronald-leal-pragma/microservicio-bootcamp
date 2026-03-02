package com.pragma.bootcamp.exceptionhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private org.springframework.http.server.RequestPath requestPath;

    private ObjectMapper objectMapper;
    private DataBufferFactory dataBufferFactory;
    private HttpHeaders httpHeaders;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        globalExceptionHandler = new GlobalExceptionHandler(objectMapper);
        dataBufferFactory = new DefaultDataBufferFactory();
        httpHeaders = new HttpHeaders();

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getPath()).thenReturn(requestPath);
        when(requestPath.toString()).thenReturn("/api/bootcamp");
        when(response.bufferFactory()).thenReturn(dataBufferFactory);
        when(response.setStatusCode(any())).thenReturn(true);
        when(response.getHeaders()).thenReturn(httpHeaders);
        when(response.writeWith(any())).thenReturn(Mono.empty());
    }

    @Test
    void testHandleResponseStatusException() {
        // Given
        ResponseStatusException responseStatusException = new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Recurso no encontrado"
        );

        // When
        Mono<Void> result = globalExceptionHandler.handle(exchange, responseStatusException);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.NOT_FOUND);
        verify(response).writeWith(any());
    }

    @Test
    void testHandleIllegalArgumentException_AlreadyExists() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("La entidad ya existe");

        // When
        Mono<Void> result = globalExceptionHandler.handle(exchange, exception);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.CONFLICT);
        verify(response).writeWith(any());
    }

    @Test
    void testHandleIllegalArgumentException_NotFound() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("La entidad no existe");

        // When
        Mono<Void> result = globalExceptionHandler.handle(exchange, exception);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.NOT_FOUND);
        verify(response).writeWith(any());
    }

    @Test
    void testHandleIllegalArgumentException_InvalidArgument() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Argumento inválido");

        // When
        Mono<Void> result = globalExceptionHandler.handle(exchange, exception);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.BAD_REQUEST);
        verify(response).writeWith(any());
    }

    @Test
    void testHandleGenericException() {
        // Given
        RuntimeException genericException = new RuntimeException("Error inesperado");

        // When
        Mono<Void> result = globalExceptionHandler.handle(exchange, genericException);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(response).writeWith(any());
    }

    @Test
    void testHandleNullPointerException() {
        // Given
        NullPointerException nullPointerException = new NullPointerException();

        // When
        Mono<Void> result = globalExceptionHandler.handle(exchange, nullPointerException);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(response).writeWith(any());
    }

    @Test
    void testHandleIllegalArgumentException_WithNotFoundKeyword() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Resource not found in database");

        // When
        Mono<Void> result = globalExceptionHandler.handle(exchange, exception);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.NOT_FOUND);
        verify(response).writeWith(any());
    }

    @Test
    void testHandleResponseStatusException_WithDifferentStatus() {
        // Given
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Acceso denegado"
        );

        // When
        Mono<Void> result = globalExceptionHandler.handle(exchange, exception);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
        verify(response).writeWith(any());
    }
}

