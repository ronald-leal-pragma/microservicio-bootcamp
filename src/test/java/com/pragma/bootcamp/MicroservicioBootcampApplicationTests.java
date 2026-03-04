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
    }
}
