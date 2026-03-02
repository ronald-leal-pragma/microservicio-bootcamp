package com.pragma.bootcamp.infrastructure.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("persona")
public class PersonaEntity {
    @Id
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String documento;
}
