package com.pragma.bootcamp.infrastructure.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("inscripcion")
public class InscripcionEntity {
    @Id
    private Long id;
    private Long personaId;
    private Long bootcampId;
    private LocalDate fechaInscripcion;
    private String estado;
}
