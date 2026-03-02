package com.pragma.bootcamp.infrastructure.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta de error estándar de la API")
public class ErrorResponse {
    @Schema(description = "Código de error", example = "VALIDATION_ERROR")
    private String code;

    @Schema(description = "Mensaje de error descriptivo", example = "El nombre de la franquicia no puede estar vacío")
    private String message;

    @Schema(description = "Fecha y hora del error", example = "2026-02-15T10:30:00")
    private LocalDateTime timestamp;
}