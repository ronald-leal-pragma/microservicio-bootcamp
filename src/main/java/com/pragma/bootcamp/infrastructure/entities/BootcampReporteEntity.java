package com.pragma.bootcamp.infrastructure.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("bootcamp_reporte")
public class BootcampReporteEntity {
    @Id
    private Long id;
    private Long bootcampId;
    private String bootcampNombre;
    private String bootcampDescripcion;
    private LocalDate fechaLanzamiento;
    private Integer duracionSemanas;
    private Integer cantidadCapacidades;
    private Integer cantidadTecnologias;
    private Integer cantidadPersonasInscritas;
    private LocalDateTime fechaRegistroReporte;
}
