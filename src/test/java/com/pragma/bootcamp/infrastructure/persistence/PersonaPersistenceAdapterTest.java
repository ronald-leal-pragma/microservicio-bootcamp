package com.pragma.bootcamp.infrastructure.persistence;

import com.pragma.bootcamp.domain.models.Persona;
import com.pragma.bootcamp.infrastructure.entities.PersonaEntity;
import com.pragma.bootcamp.infrastructure.r2dbc.IPersonaRepository;
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
@DisplayName("PersonaPersistenceAdapter - Tests Unitarios")
class PersonaPersistenceAdapterTest {

    @Mock
    private IPersonaRepository personaRepository;

    @InjectMocks
    private PersonaPersistenceAdapter personaPersistenceAdapter;

    private Persona persona;
    private PersonaEntity personaEntity;

    @BeforeEach
    void setUp() {
        persona = Persona.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan.perez@test.com")
                .documento("12345678")
                .build();

        personaEntity = PersonaEntity.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan.perez@test.com")
                .documento("12345678")
                .build();
    }

    @Test
    @DisplayName("Debe guardar persona exitosamente")
    void savePersona_Success() {
        // Arrange
        when(personaRepository.save(any(PersonaEntity.class))).thenReturn(Mono.just(personaEntity));

        // Act
        Mono<Persona> result = personaPersistenceAdapter.savePersona(persona);

        // Assert
        StepVerifier.create(result)
                .assertNext(saved -> {
                    assertThat(saved).isNotNull();
                    assertThat(saved.getNombre()).isEqualTo("Juan");
                    assertThat(saved.getEmail()).isEqualTo("juan.perez@test.com");
                })
                .verifyComplete();

        verify(personaRepository, times(1)).save(any(PersonaEntity.class));
    }

    @Test
    @DisplayName("Debe obtener persona por ID exitosamente")
    void findById_Success() {
        // Arrange
        when(personaRepository.findById(1L)).thenReturn(Mono.just(personaEntity));

        // Act
        Mono<Persona> result = personaPersistenceAdapter.findById(1L);

        // Assert
        StepVerifier.create(result)
                .assertNext(found -> {
                    assertThat(found).isNotNull();
                    assertThat(found.getId()).isEqualTo(1L);
                })
                .verifyComplete();

        verify(personaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe obtener persona por email exitosamente")
    void findByEmail_Success() {
        // Arrange
        when(personaRepository.findByEmail(anyString())).thenReturn(Mono.just(personaEntity));

        // Act
        Mono<Persona> result = personaPersistenceAdapter.findByEmail("juan.perez@test.com");

        // Assert
        StepVerifier.create(result)
                .assertNext(found -> {
                    assertThat(found).isNotNull();
                    assertThat(found.getEmail()).isEqualTo("juan.perez@test.com");
                })
                .verifyComplete();

        verify(personaRepository, times(1)).findByEmail(anyString());
    }

    @Test
    @DisplayName("Debe obtener persona por documento exitosamente")
    void findByDocumento_Success() {
        // Arrange
        when(personaRepository.findByDocumento(anyString())).thenReturn(Mono.just(personaEntity));

        // Act
        Mono<Persona> result = personaPersistenceAdapter.findByDocumento("12345678");

        // Assert
        StepVerifier.create(result)
                .assertNext(found -> {
                    assertThat(found).isNotNull();
                    assertThat(found.getDocumento()).isEqualTo("12345678");
                })
                .verifyComplete();

        verify(personaRepository, times(1)).findByDocumento(anyString());
    }

    @Test
    @DisplayName("Debe retornar vacío si persona no existe por email")
    void findByEmail_NotFound() {
        // Arrange
        when(personaRepository.findByEmail(anyString())).thenReturn(Mono.empty());

        // Act
        Mono<Persona> result = personaPersistenceAdapter.findByEmail("noexiste@test.com");

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        verify(personaRepository, times(1)).findByEmail(anyString());
    }
}
