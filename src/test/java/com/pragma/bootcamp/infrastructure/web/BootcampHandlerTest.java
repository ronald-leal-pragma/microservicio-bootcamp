package com.pragma.bootcamp.infrastructure.web;

import com.pragma.bootcamp.application.dtos.requests.BootcampRequest;
import com.pragma.bootcamp.application.dtos.responses.BootcampCompleteResponse;
import com.pragma.bootcamp.application.dtos.responses.BootcampDetalleCompletoResponse;
import com.pragma.bootcamp.application.dtos.responses.BootcampResponse;
import com.pragma.bootcamp.application.mappers.BootcampMapper;
import com.pragma.bootcamp.domain.models.Bootcamp;
import com.pragma.bootcamp.domain.ports.in.IBootcampServicePort;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BootcampHandler - Tests Unitarios")
class BootcampHandlerTest {

    @Mock
    private IBootcampServicePort bootcampServicePort;

    @Mock
    private BootcampMapper bootcampMapper;

    @Mock
    private Validator validator;

    @Mock
    private ServerRequest serverRequest;

    @InjectMocks
    private BootcampHandler bootcampHandler;

    private Bootcamp bootcamp;
    private BootcampRequest bootcampRequest;
    private BootcampResponse bootcampResponse;

    @BeforeEach
    void setUp() {
        bootcamp = Bootcamp.builder()
                .id(1L)
                .nombre("Bootcamp Test")
                .descripcion("Descripción")
                .fechaLanzamiento(LocalDate.of(2026, 6, 1))
                .duracionSemanas(12)
                .capacidadesIds(Set.of(1L, 2L))
                .build();

        bootcampRequest = new BootcampRequest();
        bootcampRequest.setNombre("Bootcamp Test");
        bootcampRequest.setCapacidadesIds(Set.of(1L, 2L));

        bootcampResponse = BootcampResponse.builder()
                .id(1L)
                .nombre("Bootcamp Test")
                .build();
    }

    @Test
    @DisplayName("Debe guardar bootcamp exitosamente")
    void saveBootcamp_Success() {
        // Arrange
        when(serverRequest.bodyToMono(BootcampRequest.class)).thenReturn(Mono.just(bootcampRequest));
        when(bootcampMapper.toDomain(any(BootcampRequest.class))).thenReturn(bootcamp);
        when(bootcampServicePort.saveBootcamp(any(Bootcamp.class))).thenReturn(Mono.just(bootcamp));
        when(bootcampMapper.toResponse(any(Bootcamp.class))).thenReturn(bootcampResponse);

        // Act
        Mono<ServerResponse> result = bootcampHandler.saveBootcamp(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();

        verify(bootcampServicePort, times(1)).saveBootcamp(any(Bootcamp.class));
    }

    @Test
    @DisplayName("Debe manejar error de bootcamp duplicado (409 Conflict)")
    void saveBootcamp_DuplicateName_ShouldReturn409() {
        // Arrange
        when(serverRequest.bodyToMono(BootcampRequest.class)).thenReturn(Mono.just(bootcampRequest));
        when(bootcampMapper.toDomain(any(BootcampRequest.class))).thenReturn(bootcamp);
        when(bootcampServicePort.saveBootcamp(any(Bootcamp.class)))
                .thenReturn(Mono.error(new DuplicateKeyException("Duplicate key")));

        // Act
        Mono<ServerResponse> result = bootcampHandler.saveBootcamp(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().value() == 409)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe manejar error de validación (400 Bad Request)")
    void saveBootcamp_ValidationError_ShouldReturn400() {
        // Arrange
        when(serverRequest.bodyToMono(BootcampRequest.class)).thenReturn(Mono.just(bootcampRequest));
        when(bootcampMapper.toDomain(any(BootcampRequest.class))).thenReturn(bootcamp);
        when(bootcampServicePort.saveBootcamp(any(Bootcamp.class)))
                .thenReturn(Mono.error(new IllegalArgumentException("Validation error")));

        // Act
        Mono<ServerResponse> result = bootcampHandler.saveBootcamp(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is4xxClientError())
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe obtener todos los bootcamps")
    void getAllBootcamps_Success() {
        // Arrange
        BootcampCompleteResponse completeResponse = BootcampCompleteResponse.builder()
                .id(1L)
                .nombre("Bootcamp Test")
                .build();

        when(serverRequest.queryParam("page")).thenReturn(Optional.of("0"));
        when(serverRequest.queryParam("size")).thenReturn(Optional.of("10"));
        when(serverRequest.queryParam("sort")).thenReturn(Optional.of("nombre"));
        when(serverRequest.queryParam("order")).thenReturn(Optional.of("asc"));
        when(bootcampServicePort.getAllBootcamps(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(Flux.just(completeResponse));

        // Act
        Mono<ServerResponse> result = bootcampHandler.getAllBootcamps(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();

        verify(bootcampServicePort, times(1)).getAllBootcamps(anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("Debe obtener bootcamp por ID")
    void getBootcampById_Success() {
        // Arrange
        when(serverRequest.pathVariable("id")).thenReturn("1");
        when(bootcampServicePort.getBootcampById(1L)).thenReturn(Mono.just(bootcamp));
        when(bootcampMapper.toResponse(any(Bootcamp.class))).thenReturn(bootcampResponse);

        // Act
        Mono<ServerResponse> result = bootcampHandler.getBootcampById(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();

        verify(bootcampServicePort, times(1)).getBootcampById(1L);
    }

    @Test
    @DisplayName("Debe retornar 404 si bootcamp no existe")
    void getBootcampById_NotFound() {
        // Arrange
        when(serverRequest.pathVariable("id")).thenReturn("999");
        when(bootcampServicePort.getBootcampById(999L)).thenReturn(Mono.empty());

        // Act
        Mono<ServerResponse> result = bootcampHandler.getBootcampById(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().value() == 404)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe eliminar bootcamp exitosamente")
    void deleteBootcamp_Success() {
        // Arrange
        when(serverRequest.pathVariable("id")).thenReturn("1");
        when(bootcampServicePort.deleteBootcamp(1L)).thenReturn(Mono.empty());

        // Act
        Mono<ServerResponse> result = bootcampHandler.deleteBootcamp(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();

        verify(bootcampServicePort, times(1)).deleteBootcamp(1L);
    }

    @Test
    @DisplayName("Debe obtener bootcamp completo con detalle")
    void getBootcampCompleto_Success() {
        // Arrange
        BootcampDetalleCompletoResponse detalleResponse = BootcampDetalleCompletoResponse.builder()
                .id(1L)
                .nombre("Bootcamp Test")
                .personasInscritas(List.of())
                .capacidades(List.of())
                .tecnologias(List.of())
                .build();

        when(serverRequest.pathVariable("id")).thenReturn("1");
        when(bootcampServicePort.getBootcampCompleto(1L)).thenReturn(Mono.just(detalleResponse));

        // Act
        Mono<ServerResponse> result = bootcampHandler.getBootcampCompleto(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();

        verify(bootcampServicePort, times(1)).getBootcampCompleto(1L);
    }
}
