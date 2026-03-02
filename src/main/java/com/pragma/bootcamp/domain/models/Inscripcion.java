package com.pragma.bootcamp.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inscripcion {
    private Long id;
    private Long personaId;
    private Long bootcampId;
    private LocalDate fechaInscripcion;
    private String estado; // ACTIVA, CANCELADA, FINALIZADA
    
    // Campos enriquecidos (no persistidos)
    private Persona persona;
    private Bootcamp bootcamp;
}
