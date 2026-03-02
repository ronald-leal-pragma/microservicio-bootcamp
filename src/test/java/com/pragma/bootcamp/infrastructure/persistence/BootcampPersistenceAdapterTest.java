package com.pragma.bootcamp.infrastructure.persistence;

import com.pragma.bootcamp.domain.models.Bootcamp;
import com.pragma.bootcamp.infrastructure.r2dbc.IBootcampRepository;
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
@DisplayName("BootcampPersistenceAdapter - Tests Unitarios")
class BootcampPersistenceAdapterTest {

    @Mock
    private IBootcampRepository bootcampRepository;

    @Mock
    private BootcampEntityMapper bootcampEntityMapper;

    @InjectMocks
    private BootcampPersistenceAdapter bootcampPersistenceAdapter;

    private Bootcamp bootcamp;
    private BootcampEntity bootcampEntity;

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

        bootcampEntity = BootcampEntity.builder()
                .id(1L)
                .nombre("Bootcamp Test")
                .descripcion("Descripción")
                .fechaLanzamiento(LocalDate.of(2026, 6, 1))
                .duracionSemanas(12)
                .capacidadesIds("[1,2]")
                .build();
    }

    @Test
    @DisplayName("Debe guardar bootcamp exitosamente")
    void save_Success() {
        // Arrange
        when(bootcampEntityMapper.toEntity(any(Bootcamp.class))).thenReturn(bootcampEntity);
        when(bootcampRepository.save(any(BootcampEntity.class))).thenReturn(Mono.just(bootcampEntity));
        when(bootcampEntityMapper.toDomain(any(BootcampEntity.class))).thenReturn(bootcamp);

        // Act
        Mono<Bootcamp> result = bootcampPersistenceAdapter.save(bootcamp);

        // Assert
        StepVerifier.create(result)
                .assertNext(saved -> {
                    assertThat(saved).isNotNull();
                    assertThat(saved.getNombre()).isEqualTo("Bootcamp Test");
                })
                .verifyComplete();

        verify(bootcampRepository, times(1)).save(any(BootcampEntity.class));
    }

    @Test
    @DisplayName("Debe obtener bootcamp por ID exitosamente")
    void getBootcampById_Success() {
        // Arrange
        when(bootcampRepository.findById(1L)).thenReturn(Mono.just(bootcampEntity));
        when(bootcampEntityMapper.toDomain(any(BootcampEntity.class))).thenReturn(bootcamp);

        // Act
        Mono<Bootcamp> result = bootcampPersistenceAdapter.getBootcampById(1L);

        // Assert
        StepVerifier.create(result)
                .assertNext(found -> {
                    assertThat(found).isNotNull();
                    assertThat(found.getId()).isEqualTo(1L);
                })
                .verifyComplete();

        verify(bootcampRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe retornar vacío si bootcamp no existe")
    void getBootcampById_NotFound() {
        // Arrange
        when(bootcampRepository.findById(999L)).thenReturn(Mono.empty());

        // Act
        Mono<Bootcamp> result = bootcampPersistenceAdapter.getBootcampById(999L);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        verify(bootcampRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe eliminar bootcamp exitosamente")
    void deleteBootcamp_Success() {
        // Arrange
        when(bootcampRepository.deleteById(1L)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = bootcampPersistenceAdapter.deleteBootcamp(1L);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(bootcampRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Debe obtener todos los bootcamps")
    void findAllBootcamps_Success() {
        // Arrange
        BootcampEntity entity2 = BootcampEntity.builder()
                .id(2L)
                .nombre("Bootcamp 2")
                .build();

        Bootcamp bootcamp2 = Bootcamp.builder()
                .id(2L)
                .nombre("Bootcamp 2")
                .build();

        when(bootcampRepository.findAll()).thenReturn(Flux.just(bootcampEntity, entity2));
        when(bootcampEntityMapper.toDomain(bootcampEntity)).thenReturn(bootcamp);
        when(bootcampEntityMapper.toDomain(entity2)).thenReturn(bootcamp2);

        // Act
        Flux<Bootcamp> result = bootcampPersistenceAdapter.findAllBootcamps();

        // Assert
        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();

        verify(bootcampRepository, times(1)).findAll();
    }
}
