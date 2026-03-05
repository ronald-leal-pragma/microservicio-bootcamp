package com.pragma.bootcamp.infrastructure.persistence;

import com.pragma.bootcamp.domain.models.BootcampReporte;
import com.pragma.bootcamp.domain.ports.out.IReporteServicePort;
import com.pragma.bootcamp.infrastructure.entities.BootcampReporteMongoEntity;
import com.pragma.bootcamp.infrastructure.mongodb.IBootcampReporteMongoRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportePersistenceAdapter implements IReporteServicePort {

    private final IBootcampReporteMongoRepository reporteRepository;

    @Override
    @CircuitBreaker(name = "reporte-mongo", fallbackMethod = "generarReporteFallback")
    public Mono<BootcampReporte> generarReporte(BootcampReporte reporte) {
        log.info("ReportePersistenceAdapter: Guardando reporte en MongoDB para bootcamp ID: {}",
                reporte.getBootcampId());

        BootcampReporteMongoEntity entity = toEntity(reporte);
        return reporteRepository.save(entity)
                .map(this::toDomain)
                .doOnSuccess(r -> log.info("ReportePersistenceAdapter: Reporte guardado con ID: {}", r.getId()))
                .doOnError(e -> log.error("ReportePersistenceAdapter: Error guardando reporte: {}", e.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "reporte-mongo", fallbackMethod = "actualizarReporteFallback")
    public Mono<Void> actualizarReporte(Long bootcampId, int cantPersonas,
                                        int cantCapacidades, int cantTecnologias) {
        log.info("ReportePersistenceAdapter: Actualizando reporte en MongoDB para bootcamp ID: {}", bootcampId);

        String bootcampIdStr = String.valueOf(bootcampId);

        return reporteRepository.findFirstByBootcampIdOrderByFechaGeneracionDesc(bootcampIdStr)
                .flatMap(entity -> {
                    entity.setCantidadPersonasInscritas(cantPersonas);
                    entity.setCantidadCapacidades(cantCapacidades);
                    entity.setCantidadTecnologias(cantTecnologias);
                    entity.setMetricas(buildMetricas(cantPersonas, cantCapacidades, cantTecnologias));
                    entity.setFechaGeneracion(LocalDateTime.now());
                    return reporteRepository.save(entity);
                })
                .doOnSuccess(r -> log.info("ReportePersistenceAdapter: Reporte actualizado para bootcamp ID: {}", bootcampId))
                .doOnError(e -> log.error("ReportePersistenceAdapter: Error actualizando reporte: {}", e.getMessage()))
                .then();
    }

    // ===== MAPPERS =====

    private BootcampReporteMongoEntity toEntity(BootcampReporte domain) {
        BootcampReporteMongoEntity entity = new BootcampReporteMongoEntity();
        entity.setBootcampId(domain.getBootcampId() != null ? String.valueOf(domain.getBootcampId()) : null);
        entity.setBootcampNombre(domain.getBootcampNombre());
        entity.setBootcampDescripcion(domain.getBootcampDescripcion());
        entity.setCantidadPersonasInscritas(
                domain.getCantidadPersonasInscritas() != null ? domain.getCantidadPersonasInscritas() : 0);
        entity.setCantidadCapacidades(
                domain.getCantidadCapacidades() != null ? domain.getCantidadCapacidades() : 0);
        entity.setCantidadTecnologias(
                domain.getCantidadTecnologias() != null ? domain.getCantidadTecnologias() : 0);
        entity.setMetricas(buildMetricas(
                domain.getCantidadPersonasInscritas() != null ? domain.getCantidadPersonasInscritas() : 0,
                domain.getCantidadCapacidades() != null ? domain.getCantidadCapacidades() : 0,
                domain.getCantidadTecnologias() != null ? domain.getCantidadTecnologias() : 0));
        entity.setFechaGeneracion(domain.getFechaRegistroReporte() != null
                ? domain.getFechaRegistroReporte()
                : LocalDateTime.now());
        return entity;
    }

    private BootcampReporte toDomain(BootcampReporteMongoEntity entity) {
        return BootcampReporte.builder()
                .bootcampId(entity.getBootcampId() != null ? Long.parseLong(entity.getBootcampId()) : null)
                .bootcampNombre(entity.getBootcampNombre())
                .bootcampDescripcion(entity.getBootcampDescripcion())
                .cantidadPersonasInscritas(entity.getCantidadPersonasInscritas())
                .cantidadCapacidades(entity.getCantidadCapacidades())
                .cantidadTecnologias(entity.getCantidadTecnologias())
                .fechaRegistroReporte(entity.getFechaGeneracion())
                .build();
    }

    private Map<String, Object> buildMetricas(int cantPersonas, int cantCapacidades, int cantTecnologias) {
        Map<String, Object> metricas = new HashMap<>();
        metricas.put("promedioPersonasPorCapacidad",
                cantCapacidades > 0 ? (double) cantPersonas / cantCapacidades : 0.0);
        metricas.put("promedioTecnologiasPorCapacidad",
                cantCapacidades > 0 ? (double) cantTecnologias / cantCapacidades : 0.0);
        metricas.put("tieneInscritos", cantPersonas > 0);
        metricas.put("tieneCapacidades", cantCapacidades > 0);
        return metricas;
    }

    // ===== CIRCUIT BREAKER FALLBACK METHODS =====

    private Mono<BootcampReporte> generarReporteFallback(BootcampReporte reporte, Exception ex) {
        log.warn("ReportePersistenceAdapter: Fallback generarReporte - MongoDB no disponible: {}", ex.getMessage());
        return Mono.empty();
    }

    private Mono<Void> actualizarReporteFallback(Long bootcampId, int cantPersonas,
                                                   int cantCapacidades, int cantTecnologias,
                                                   Exception ex) {
        log.warn("ReportePersistenceAdapter: Fallback actualizarReporte para bootcamp {} - MongoDB no disponible: {}",
                bootcampId, ex.getMessage());
        return Mono.empty();
    }
}
