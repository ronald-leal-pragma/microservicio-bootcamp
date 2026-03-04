package com.pragma.bootcamp.infrastructure.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table("bootcamp")
public class BootcampEntity {
    @Id
    private Long id;
    private String nombre;
    private String descripcion;
    @Column("fecha_lanzamiento")
    private LocalDate fechaLanzamiento;
    @Column("duracion_semanas")
    private Integer duracionSemanas;
    @Column("capacidades_ids")
    private String capacidadesIds;
}
