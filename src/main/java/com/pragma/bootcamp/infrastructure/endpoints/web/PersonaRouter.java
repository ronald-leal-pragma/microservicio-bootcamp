package com.pragma.bootcamp.infrastructure.endpoints.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class PersonaRouter {

    @Bean
    public RouterFunction<ServerResponse> personaRoutes(PersonaHandler personaHandler) {
        return RouterFunctions
                .route(POST("/api/persona").and(accept(MediaType.APPLICATION_JSON)), personaHandler::createPersona)
                .andRoute(GET("/api/persona/{id}"), personaHandler::getPersonaById);
    }
}
