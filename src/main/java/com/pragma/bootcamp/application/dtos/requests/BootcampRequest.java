package com.pragma.bootcamp.application.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Schema(description = "Modelo para la creación de un nuevo Bootcamp")
public class BootcampRequest {
    @NotBlank
    @Size(max = 100)
    @Schema(description = "Nombre del bootcamp", example = "Java Fullstack")
    private String nombre;

    @NotBlank
    @Size(max = 250)
    @Schema(description = "Lista de IDs de capacidades asociadas", example = "[1, 2, 3]")
    private String descripcion;

    @NotNull
    @Schema(
            description = "Fecha en la que iniciará el bootcamp",
            example = "2024-06-01",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate fechaLanzamiento;

    @NotNull
    @Min(1)
    @Schema(
            description = "Duración total del programa en semanas",
            example = "12",
            minimum = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer duracionSemanas;

    @NotNull
    @Schema(
            description = "Conjunto de IDs de las capacidades que componen este bootcamp",
            example = "[1, 2, 5]",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Set<Long> capacidadesIds;
}
