package com.pragma.bootcamp.infrastructure.endpoints.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String documento;
}
