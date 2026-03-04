package com.pragma.bootcamp.application.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta paginada genérica")
public class PagedResponse<T> {
    
    @Schema(description = "Contenido de la página actual")
    private List<T> content;
    
    @Schema(description = "Metadata de paginación")
    private PageMetadata metadata;
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Metadata de paginación")
    public static class PageMetadata {
        
        @Schema(description = "Número de página actual (base 0)", example = "0")
        private int page;
        
        @Schema(description = "Tamaño de la página", example = "10")
        private int size;
        
        @Schema(description = "Total de elementos", example = "100")
        private long totalElements;
        
        @Schema(description = "Total de páginas", example = "10")
        private int totalPages;
        
        @Schema(description = "Campo de ordenamiento", example = "nombre")
        private String sortBy;
        
        @Schema(description = "Dirección de ordenamiento", example = "asc")
        private String sortOrder;
    }
}
