package com.pragma.bootcamp.infrastructure.endpoints.web;

import com.pragma.bootcamp.application.dtos.responses.BootcampCompleteResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionResponse {
    private Long id;
    private Long personaId;
    private Long bootcampId;
    private LocalDate fechaInscripcion;
    private String estado;
    private PersonaResponse persona;
    private BootcampCompleteResponse bootcamp;
}
