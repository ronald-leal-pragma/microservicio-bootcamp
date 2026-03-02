package com.pragma.bootcamp.infrastructure.endpoints.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class InscripcionRouter {

    @Bean
    public RouterFunction<ServerResponse> inscripcionRoutes(InscripcionHandler inscripcionHandler) {
        return RouterFunctions
                .route(POST("/api/inscripcion").and(accept(MediaType.APPLICATION_JSON)), inscripcionHandler::inscribirPersona)
                .andRoute(GET("/api/inscripcion/{id}"), inscripcionHandler::getInscripcionById)
                .andRoute(GET("/api/inscripcion/persona/{personaId}"), inscripcionHandler::getInscripcionesByPersonaId);
    }
}
