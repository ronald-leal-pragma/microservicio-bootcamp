package com.pragma.bootcamp.application.usecase;

import com.pragma.bootcamp.domain.models.Bootcamp;
import com.pragma.bootcamp.domain.models.Inscripcion;
import com.pragma.bootcamp.domain.models.Persona;
import com.pragma.bootcamp.domain.ports.in.IBootcampServicePort;
import com.pragma.bootcamp.domain.ports.out.IInscripcionPersistencePort;
import com.pragma.bootcamp.domain.ports.out.IPersonaServicePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InscripcionUseCase - Tests Unitarios")
class InscripcionUseCaseTest {

    @Mock
    private IInscripcionPersistencePort inscripcionPersistencePort;

    @Mock
    private IPersonaServicePort personaServicePort;

    @Mock
    private IBootcampServicePort bootcampServicePort;

    @InjectMocks
    private InscripcionUseCase inscripcionUseCase;

    private Persona persona;
    private Bootcamp bootcamp;
    private Inscripcion inscripcion;

    @BeforeEach
    void setUp() {
        persona = Persona.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan.perez@test.com")
                .documento("12345678")
                .build();

        bootcamp = Bootcamp.builder()
                .id(1L)
                .nombre("Bootcamp Test")
                .descripcion("Descripción")
                .fechaLanzamiento(LocalDate.of(2026, 6, 1))
                .duracionSemanas(12)
                .capacidadesIds(Set.of(1L, 2L))
                .build();

        inscripcion = Inscripcion.builder()
                .id(1L)
                .personaId(1L)
                .bootcampId(1L)
                .fechaInscripcion(LocalDate.now())
                .estado("ACTIVA")
                .build();
    }

    @Test
    @DisplayName("Debe inscribir persona en bootcamp exitosamente")
    void inscribirPersonaEnBootcamp_Success() {
        // Arrange
        when(personaServicePort.findById(1L)).thenReturn(Mono.just(persona));
        when(bootcampServicePort.getBootcampById(1L)).thenReturn(Mono.just(bootcamp));
        when(inscripcionPersistencePort.countInscripcionesActivasByPersonaId(1L)).thenReturn(Mono.just(0L));
        when(inscripcionPersistencePort.findByPersonaIdActivas(1L)).thenReturn(Flux.empty());
        when(inscripcionPersistencePort.saveInscripcion(any(Inscripcion.class))).thenReturn(Mono.just(inscripcion));

        // Act
        Mono<Inscripcion> result = inscripcionUseCase.inscribirPersonaEnBootcamp(1L, 1L);

        // Assert
        StepVerifier.create(result)
                .assertNext(saved -> {
                    assertThat(saved).isNotNull();
                    assertThat(saved.getPersonaId()).isEqualTo(1L);
                    assertThat(saved.getBootcampId()).isEqualTo(1L);
                    assertThat(saved.getEstado()).isEqualTo("ACTIVA");
                })
                .verifyComplete();

        verify(inscripcionPersistencePort, times(1)).saveInscripcion(any(Inscripcion.class));
    }

    @Test
    @DisplayName("Debe fallar si persona no existe")
    void inscribirPersonaEnBootcamp_PersonaNotFound_ShouldFail() {
        // Arrange
        when(personaServicePort.findById(999L)).thenReturn(Mono.empty());

        // Act
        Mono<Inscripcion> result = inscripcionUseCase.inscribirPersonaEnBootcamp(999L, 1L);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof IllegalArgumentException &&
                    throwable.getMessage().contains("La persona con ID 999 no existe")
                )
                .verify();

        verify(inscripcionPersistencePort, never()).saveInscripcion(any(Inscripcion.class));
    }

    @Test
    @DisplayName("Debe fallar si bootcamp no existe")
    void inscribirPersonaEnBootcamp_BootcampNotFound_ShouldFail() {
        // Arrange
        when(personaServicePort.findById(1L)).thenReturn(Mono.just(persona));
        when(bootcampServicePort.getBootcampById(999L)).thenReturn(Mono.empty());

        // Act
        Mono<Inscripcion> result = inscripcionUseCase.inscribirPersonaEnBootcamp(1L, 999L);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof IllegalArgumentException &&
                    throwable.getMessage().contains("El bootcamp con ID 999 no existe")
                )
                .verify();

        verify(inscripcionPersistencePort, never()).saveInscripcion(any(Inscripcion.class));
    }

    @Test
    @DisplayName("Debe fallar si persona ya tiene 5 inscripciones activas")
    void inscribirPersonaEnBootcamp_MaxInscripciones_ShouldFail() {
        // Arrange
        when(personaServicePort.findById(1L)).thenReturn(Mono.just(persona));
        when(bootcampServicePort.getBootcampById(1L)).thenReturn(Mono.just(bootcamp));
        when(inscripcionPersistencePort.countInscripcionesActivasByPersonaId(1L)).thenReturn(Mono.just(5L));

        // Act
        Mono<Inscripcion> result = inscripcionUseCase.inscribirPersonaEnBootcamp(1L, 1L);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof IllegalArgumentException &&
                    throwable.getMessage().contains("ya tiene el máximo de 5 inscripciones activas")
                )
                .verify();

        verify(inscripcionPersistencePort, never()).saveInscripcion(any(Inscripcion.class));
    }

    @Test
    @DisplayName("Debe fallar si hay solapamiento de fechas con otra inscripción")
    void inscribirPersonaEnBootcamp_FechasSuperpuestas_ShouldFail() {
        // Arrange
        Bootcamp bootcampExistente = Bootcamp.builder()
                .id(2L)
                .nombre("Bootcamp Existente")
                .descripcion("Descripción")
                .fechaLanzamiento(LocalDate.of(2026, 5, 15)) // Solapa con el nuevo (2026-06-01)
                .duracionSemanas(8) // Termina 2026-07-10
                .capacidadesIds(Set.of(3L))
                .build();

        Inscripcion inscripcionExistente = Inscripcion.builder()
                .id(2L)
                .personaId(1L)
                .bootcampId(2L)
                .fechaInscripcion(LocalDate.now())
                .estado("ACTIVA")
                .build();

        when(personaServicePort.findById(1L)).thenReturn(Mono.just(persona));
        when(bootcampServicePort.getBootcampById(1L)).thenReturn(Mono.just(bootcamp));
        when(inscripcionPersistencePort.countInscripcionesActivasByPersonaId(1L)).thenReturn(Mono.just(1L));
        when(inscripcionPersistencePort.findByPersonaIdActivas(1L)).thenReturn(Flux.just(inscripcionExistente));
        when(bootcampServicePort.getBootcampById(2L)).thenReturn(Mono.just(bootcampExistente));

        // Act
        Mono<Inscripcion> result = inscripcionUseCase.inscribirPersonaEnBootcamp(1L, 1L);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof IllegalArgumentException &&
                    throwable.getMessage().contains("fechas que se solapan")
                )
                .verify();

        verify(inscripcionPersistencePort, never()).saveInscripcion(any(Inscripcion.class));
    }

    @Test
    @DisplayName("Debe obtener inscripciones por persona ID")
    void getInscripcionesByPersonaId_Success() {
        // Arrange
        when(inscripcionPersistencePort.findByPersonaId(1L)).thenReturn(Flux.just(inscripcion));
        when(personaServicePort.findById(1L)).thenReturn(Mono.just(persona));
        when(bootcampServicePort.getBootcampById(1L)).thenReturn(Mono.just(bootcamp));

        // Act
        Flux<Inscripcion> result = inscripcionUseCase.getInscripcionesByPersonaId(1L);

        // Assert
        StepVerifier.create(result)
                .assertNext(found -> {
                    assertThat(found).isNotNull();
                    assertThat(found.getPersonaId()).isEqualTo(1L);
                })
                .verifyComplete();

        verify(inscripcionPersistencePort, times(1)).findByPersonaId(1L);
    }

    @Test
    @DisplayName("Debe obtener inscripción por ID")
    void getInscripcionById_Success() {
        // Arrange
        when(inscripcionPersistencePort.findById(1L)).thenReturn(Mono.just(inscripcion));
        when(personaServicePort.findById(1L)).thenReturn(Mono.just(persona));
        when(bootcampServicePort.getBootcampById(1L)).thenReturn(Mono.just(bootcamp));

        // Act
        Mono<Inscripcion> result = inscripcionUseCase.getInscripcionById(1L);

        // Assert
        StepVerifier.create(result)
                .assertNext(found -> {
                    assertThat(found).isNotNull();
                    assertThat(found.getId()).isEqualTo(1L);
                })
                .verifyComplete();

        verify(inscripcionPersistencePort, times(1)).findById(1L);
    }
}
