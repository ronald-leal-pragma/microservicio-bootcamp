package com.pragma.bootcamp.application.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CapacidadSimpleResponse {
    private Long id;
    private String nombre;
    private List<TecnologiaSimpleResponse> tecnologias;
}
