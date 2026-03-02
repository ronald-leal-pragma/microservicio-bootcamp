package com.pragma.bootcamp.infrastructure.persistence;

import com.pragma.bootcamp.domain.models.BootcampReporte;
import com.pragma.bootcamp.domain.ports.out.IBootcampReportePersistencePort;
import com.pragma.bootcamp.infrastructure.entities.BootcampReporteEntity;
import com.pragma.bootcamp.infrastructure.r2dbc.IBootcampReporteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootcampReportePersistenceAdapter implements IBootcampReportePersistencePort {

    private final IBootcampReporteRepository reporteRepository;

    @Override
    public Mono<BootcampReporte> saveReporte(BootcampReporte reporte) {
        log.info("ReporteAdapter: Guardando reporte para bootcamp ID: {}", reporte.getBootcampId());
        BootcampReporteEntity entity = toEntity(reporte);
        return reporteRepository.save(entity)
                .map(this::toModel)
                .doOnSuccess(saved -> log.info("ReporteAdapter: Reporte guardado con ID: {}", saved.getId()))
                .doOnError(error -> log.error("ReporteAdapter: Error al guardar reporte: {}", error.getMessage()));
    }

    private BootcampReporteEntity toEntity(BootcampReporte reporte) {
        return BootcampReporteEntity.builder()
                .id(reporte.getId())
                .bootcampId(reporte.getBootcampId())
                .bootcampNombre(reporte.getBootcampNombre())
                .bootcampDescripcion(reporte.getBootcampDescripcion())
                .fechaLanzamiento(reporte.getFechaLanzamiento())
                .duracionSemanas(reporte.getDuracionSemanas())
                .cantidadCapacidades(reporte.getCantidadCapacidades())
                .cantidadTecnologias(reporte.getCantidadTecnologias())
                .cantidadPersonasInscritas(reporte.getCantidadPersonasInscritas())
                .fechaRegistroReporte(reporte.getFechaRegistroReporte())
                .build();
    }

    private BootcampReporte toModel(BootcampReporteEntity entity) {
        return BootcampReporte.builder()
                .id(entity.getId())
                .bootcampId(entity.getBootcampId())
                .bootcampNombre(entity.getBootcampNombre())
                .bootcampDescripcion(entity.getBootcampDescripcion())
                .fechaLanzamiento(entity.getFechaLanzamiento())
                .duracionSemanas(entity.getDuracionSemanas())
                .cantidadCapacidades(entity.getCantidadCapacidades())
                .cantidadTecnologias(entity.getCantidadTecnologias())
                .cantidadPersonasInscritas(entity.getCantidadPersonasInscritas())
                .fechaRegistroReporte(entity.getFechaRegistroReporte())
                .build();
    }
}
