package com.pragma.bootcamp.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BootcampReporte {
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
