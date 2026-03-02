package com.pragma.bootcamp.application.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapacidadCompleteResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private List<TecnologiaResponse> tecnologias;
}
