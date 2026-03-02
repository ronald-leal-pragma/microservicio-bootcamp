package com.pragma.bootcamp.application.usecase;

import com.pragma.bootcamp.domain.models.Persona;
import com.pragma.bootcamp.domain.ports.out.IPersonaPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonaUseCase - Tests Unitarios")
class PersonaUseCaseTest {

    @Mock
    private IPersonaPersistencePort personaPersistencePort;

    @InjectMocks
    private PersonaUseCase personaUseCase;

    private Persona persona;

    @BeforeEach
    void setUp() {
        persona = Persona.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan.perez@test.com")
                .documento("12345678")
                .build();
    }

    @Test
    @DisplayName("Debe crear persona exitosamente cuando email y documento no existen")
    void createPersona_Success() {
        // Arrange
        when(personaPersistencePort.findByEmail(anyString())).thenReturn(Mono.empty());
        when(personaPersistencePort.findByDocumento(anyString())).thenReturn(Mono.empty());
        when(personaPersistencePort.savePersona(any(Persona.class))).thenReturn(Mono.just(persona));

        // Act
        Mono<Persona> result = personaUseCase.createPersona(persona);

        // Assert
        StepVerifier.create(result)
                .assertNext(saved -> {
                    assertThat(saved).isNotNull();
                    assertThat(saved.getNombre()).isEqualTo("Juan");
                    assertThat(saved.getEmail()).isEqualTo("juan.perez@test.com");
                })
                .verifyComplete();

        verify(personaPersistencePort, times(1)).savePersona(any(Persona.class));
    }

    @Test
    @DisplayName("Debe fallar al crear persona si email ya existe")
    void createPersona_EmailExists_ShouldFail() {
        // Arrange
        when(personaPersistencePort.findByEmail(anyString())).thenReturn(Mono.just(persona));

        // Act
        Mono<Persona> result = personaUseCase.createPersona(persona);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof IllegalArgumentException &&
                    throwable.getMessage().contains("El email ya está registrado")
                )
                .verify();
    }

    @Test
    @DisplayName("Debe fallar al crear persona si documento ya existe")
    void createPersona_DocumentoExists_ShouldFail() {
        // Arrange
        when(personaPersistencePort.findByEmail(anyString())).thenReturn(Mono.empty());
        when(personaPersistencePort.findByDocumento(anyString())).thenReturn(Mono.just(persona));

        // Act
        Mono<Persona> result = personaUseCase.createPersona(persona);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof IllegalArgumentException &&
                    throwable.getMessage().contains("El documento ya está registrado")
                )
                .verify();
    }

    @Test
    @DisplayName("Debe obtener persona por ID exitosamente")
    void getPersonaById_Success() {
        // Arrange
        when(personaPersistencePort.findById(1L)).thenReturn(Mono.just(persona));

        // Act
        Mono<Persona> result = personaUseCase.getPersonaById(1L);

        // Assert
        StepVerifier.create(result)
                .assertNext(found -> {
                    assertThat(found).isNotNull();
                    assertThat(found.getId()).isEqualTo(1L);
                    assertThat(found.getNombre()).isEqualTo("Juan");
                })
                .verifyComplete();

        verify(personaPersistencePort, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe retornar vacío si persona no existe")
    void getPersonaById_NotFound() {
        // Arrange
        when(personaPersistencePort.findById(999L)).thenReturn(Mono.empty());

        // Act
        Mono<Persona> result = personaUseCase.getPersonaById(999L);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        verify(personaPersistencePort, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe obtener persona por email exitosamente")
    void getPersonaByEmail_Success() {
        // Arrange
        when(personaPersistencePort.findByEmail("juan.perez@test.com")).thenReturn(Mono.just(persona));

        // Act
        Mono<Persona> result = personaUseCase.getPersonaByEmail("juan.perez@test.com");

        // Assert
        StepVerifier.create(result)
                .assertNext(found -> {
                    assertThat(found).isNotNull();
                    assertThat(found.getEmail()).isEqualTo("juan.perez@test.com");
                })
                .verifyComplete();

        verify(personaPersistencePort, times(1)).findByEmail("juan.perez@test.com");
    }

    @Test
    @DisplayName("Debe retornar vacío si email no existe")
    void getPersonaByEmail_NotFound() {
        // Arrange
        when(personaPersistencePort.findByEmail("noexiste@test.com")).thenReturn(Mono.empty());

        // Act
        Mono<Persona> result = personaUseCase.getPersonaByEmail("noexiste@test.com");

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        verify(personaPersistencePort, times(1)).findByEmail("noexiste@test.com");
    }
}
