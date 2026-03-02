package com.pragma.bootcamp;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MicroservicioBootcamp - Tests de Aplicación")
class MicroservicioBootcampApplicationTests {

    @Test
    @Disabled("Requiere configuración de base de datos activa para ejecutarse")
    @DisplayName("Debe cargar contexto de Spring exitosamente")
    void contextLoads() {
        // Este test verifica que el contexto de Spring se carga correctamente
        // Si falla, indica problemas en la configuración de beans
        // Para ejecutarlo, se necesita configurar una base de datos R2DBC activa
    }
}
