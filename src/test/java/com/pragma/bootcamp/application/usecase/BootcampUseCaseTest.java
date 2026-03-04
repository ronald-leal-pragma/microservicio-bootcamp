package com.pragma.bootcamp.application.usecase;

import com.pragma.bootcamp.application.dtos.responses.BootcampCompleteResponse;
import com.pragma.bootcamp.application.dtos.responses.BootcampDetalleCompletoResponse;
import com.pragma.bootcamp.application.dtos.responses.CapacidadSimpleResponse;
import com.pragma.bootcamp.application.dtos.responses.TecnologiaSimpleResponse;
import com.pragma.bootcamp.domain.models.Bootcamp;
import com.pragma.bootcamp.domain.models.BootcampReporte;
import com.pragma.bootcamp.domain.models.Persona;
import com.pragma.bootcamp.domain.ports.out.IBootcampPersistencePort;
import com.pragma.bootcamp.domain.ports.out.IReporteServicePort;
import com.pragma.bootcamp.domain.ports.out.ICapacidadServicePort;
import com.pragma.bootcamp.infrastructure.entities.InscripcionEntity;
import com.pragma.bootcamp.infrastructure.r2dbc.IInscripcionRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BootcampUseCase - Tests Unitarios")
class BootcampUseCaseTest {

    @Mock
    private IBootcampPersistencePort bootcampPersistencePort;

    @Mock
    private ICapacidadServicePort capacidadServicePort;

    @Mock
    private IReporteServicePort reporteServicePort;

    @Mock
    private IInscripcionRepository inscripcionRepository;

    @Mock
    private com.pragma.bootcamp.domain.ports.out.IPersonaServicePort personaServicePort;

    @InjectMocks
    private BootcampUseCase bootcampUseCase;

    private Bootcamp bootcamp;
    private Set<Long> capacidadesIds;

    @BeforeEach
    void setUp() {
        capacidadesIds = new HashSet<>(Set.of(1L, 2L));
        bootcamp = Bootcamp.builder()
                .id(1L)
                .nombre("Bootcamp Test")
                .descripcion("Descripción de prueba")
                .fechaLanzamiento(LocalDate.of(2026, 6, 1))
                .duracionSemanas(12)
                .capacidadesIds(capacidadesIds)
                .build();
    }

    @Test
    @DisplayName("Debe guardar bootcamp exitosamente con 2 capacidades válidas")
    void saveBootcamp_Success() {
        // Arrange
        when(capacidadServicePort.validateCapacidadExists(anyLong())).thenReturn(Mono.empty());
        when(bootcampPersistencePort.save(any(Bootcamp.class))).thenReturn(Mono.just(bootcamp));
        when(inscripcionRepository.countByBootcampIdAndEstadoActiva(anyLong())).thenReturn(Mono.just(0L));
        when(capacidadServicePort.getCapacidadesByIds(anySet())).thenReturn(Flux.empty());
        when(reporteServicePort.generarReporte(any(BootcampReporte.class))).thenReturn(Mono.just(BootcampReporte.builder().build()));

        // Act
        Mono<Bootcamp> result = bootcampUseCase.saveBootcamp(bootcamp);

        // Assert
        StepVerifier.create(result)
                .assertNext(saved -> {
                    assertThat(saved).isNotNull();
                    assertThat(saved.getNombre()).isEqualTo("Bootcamp Test");
                    assertThat(saved.getCapacidadesIds()).hasSize(2);
                })
                .verifyComplete();

        verify(bootcampPersistencePort, times(1)).save(any(Bootcamp.class));
        verify(capacidadServicePort, times(2)).validateCapacidadExists(anyLong());
    }

    @Test
    @DisplayName("Debe fallar si bootcamp no tiene capacidades")
    void saveBootcamp_WithoutCapacidades_ShouldFail() {
        // Arrange
        bootcamp.setCapacidadesIds(new HashSet<>());

        // Act
        Mono<Bootcamp> result = bootcampUseCase.saveBootcamp(bootcamp);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof IllegalArgumentException &&
                    throwable.getMessage().contains("Un bootcamp debe tener entre 1 y 4 capacidades asociadas")
                )
                .verify();

        verify(bootcampPersistencePort, never()).save(any(Bootcamp.class));
    }

    @Test
    @DisplayName("Debe fallar si bootcamp tiene más de 4 capacidades")
    void saveBootcamp_WithMoreThan4Capacidades_ShouldFail() {
        // Arrange
        bootcamp.setCapacidadesIds(new HashSet<>(Set.of(1L, 2L, 3L, 4L, 5L)));

        // Act
        Mono<Bootcamp> result = bootcampUseCase.saveBootcamp(bootcamp);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof IllegalArgumentException &&
                    throwable.getMessage().contains("Un bootcamp debe tener entre 1 y 4 capacidades asociadas")
                )
                .verify();

        verify(bootcampPersistencePort, never()).save(any(Bootcamp.class));
    }

    @Test
    @DisplayName("Debe obtener bootcamp por ID exitosamente")
    void getBootcampById_Success() {
        // Arrange
        when(bootcampPersistencePort.getBootcampById(1L)).thenReturn(Mono.just(bootcamp));

        // Act
        Mono<Bootcamp> result = bootcampUseCase.getBootcampById(1L);

        // Assert
        StepVerifier.create(result)
                .assertNext(found -> {
                    assertThat(found).isNotNull();
                    assertThat(found.getId()).isEqualTo(1L);
                    assertThat(found.getNombre()).isEqualTo("Bootcamp Test");
                })
                .verifyComplete();

        verify(bootcampPersistencePort, times(1)).getBootcampById(1L);
    }

    @Test
    @DisplayName("Debe lanzar error cuando bootcamp no existe")
    void getBootcampById_NotFound() {
        // Arrange
        when(bootcampPersistencePort.getBootcampById(999L)).thenReturn(Mono.empty());

        // Act
        Mono<Bootcamp> result = bootcampUseCase.getBootcampById(999L);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        verify(bootcampPersistencePort, times(1)).getBootcampById(999L);
    }

    @Test
    @DisplayName("Debe obtener bootcamp completo con personas, capacidades y tecnologías")
    void getBootcampCompleto_Success() {
        // Arrange
        InscripcionEntity inscripcion = InscripcionEntity.builder()
                .id(1L)
                .personaId(1L)
                .bootcampId(1L)
                .estado("ACTIVA")
                .build();

        Persona persona = Persona.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan.perez@test.com")
                .build();

        TecnologiaSimpleResponse tech1 = new TecnologiaSimpleResponse(1L, "Java");
        CapacidadSimpleResponse capacidad = new CapacidadSimpleResponse(1L, "Backend", List.of(tech1));

        when(bootcampPersistencePort.getBootcampById(1L)).thenReturn(Mono.just(bootcamp));
        when(inscripcionRepository.findByBootcampIdAndEstadoActiva(1L)).thenReturn(Flux.just(inscripcion));
        when(personaServicePort.findById(1L)).thenReturn(Mono.just(persona));
        when(capacidadServicePort.getCapacidadesByIds(anySet())).thenReturn(Flux.just(capacidad));

        // Act
        Mono<BootcampDetalleCompletoResponse> result = bootcampUseCase.getBootcampCompleto(1L);

        // Assert
        StepVerifier.create(result)
                .assertNext(detalle -> {
                    assertThat(detalle).isNotNull();
                    assertThat(detalle.getNombre()).isEqualTo("Bootcamp Test");
                    assertThat(detalle.getPersonasInscritas()).hasSize(1);
                    assertThat(detalle.getPersonasInscritas().get(0).getNombre()).isEqualTo("Juan");
                    assertThat(detalle.getCapacidades()).hasSize(1);
                    assertThat(detalle.getTecnologias()).hasSize(1);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe fallar al obtener bootcamp completo si no existe")
    void getBootcampCompleto_NotFound() {
        // Arrange
        when(bootcampPersistencePort.getBootcampById(999L)).thenReturn(Mono.empty());

        // Act
        Mono<BootcampDetalleCompletoResponse> result = bootcampUseCase.getBootcampCompleto(999L);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().contains("Bootcamp no encontrado")
                )
                .verify();
    }

    @Test
    @DisplayName("Debe eliminar bootcamp exitosamente")
    void deleteBootcamp_Success() {
        // Arrange
        when(bootcampPersistencePort.getBootcampById(1L)).thenReturn(Mono.just(bootcamp));
        when(bootcampPersistencePort.deleteBootcamp(1L)).thenReturn(Mono.empty());
        when(bootcampPersistencePort.findAllBootcamps()).thenReturn(Flux.empty());
        when(capacidadServicePort.deleteCapacidad(anyLong())).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = bootcampUseCase.deleteBootcamp(1L);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(bootcampPersistencePort, times(1)).getBootcampById(1L);
        verify(bootcampPersistencePort, times(1)).deleteBootcamp(1L);
    }

    @Test
    @DisplayName("Debe listar bootcamps con paginación y ordenamiento")
    void getAllBootcamps_WithPagination() {
        // Arrange
        Bootcamp bootcamp2 = Bootcamp.builder()
                .id(2L)
                .nombre("Bootcamp 2")
                .descripcion("Descripción 2")
                .fechaLanzamiento(LocalDate.of(2026, 7, 1))
                .duracionSemanas(10)
                .capacidadesIds(Set.of(3L))
                .build();

        CapacidadSimpleResponse capacidad1 = new CapacidadSimpleResponse(1L, "Capacidad 1", List.of());

        when(bootcampPersistencePort.findAll(0, 10)).thenReturn(Flux.just(bootcamp, bootcamp2));
        when(capacidadServicePort.getCapacidadesByIds(anySet())).thenReturn(Flux.just(capacidad1));

        // Act
        Flux<BootcampCompleteResponse> result = bootcampUseCase.getAllBootcamps(0, 10, "nombre", "asc");

        // Assert
        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();

        verify(bootcampPersistencePort, times(1)).findAll(0, 10);
    }
}
