package com.pragma.bootcamp.infrastructure.web;

import com.pragma.bootcamp.application.dtos.requests.BootcampRequest;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.function.Consumer;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

@Configuration
public class BootcampRouter {

    private static final String TAG_BOOTCAMP = "Bootcamps";

    @Bean
    public RouterFunction<ServerResponse> bootcampRoutes(BootcampHandler handler) {
        return route()
                // 1. Crear Bootcamp
                .POST("/api/bootcamp", handler::saveBootcamp, docSaveBootcamp())

                // 2. Listar todos los Bootcamps
                .GET("/api/bootcamp", handler::getAllBootcamps, docGetAllBootcamps())

                // 3. Obtener Bootcamp por ID
                .GET("/api/bootcamp/{id}", handler::getBootcampById, docGetBootcampById())

                // 4. Obtener Bootcamp con detalle completo (HU8)
                .GET("/api/bootcamp/{id}/completo", handler::getBootcampCompleto, docGetBootcampCompleto())

                // 5. Eliminar Bootcamp
                .DELETE("/api/bootcamp/{id}", handler::deleteBootcamp, docDeleteBootcamp())

                .build();
    }

    private Consumer<Builder> docSaveBootcamp() {
        return ops -> ops.tag(TAG_BOOTCAMP)
                .operationId("saveBootcamp")
                .summary("Registrar un nuevo Bootcamp")
                .description("Crea un programa de entrenamiento intensivo asociando capacidades.")
                .requestBody(requestBodyBuilder().implementation(BootcampRequest.class).required(true))
                .response(responseBuilder().responseCode("201").description("Bootcamp creado con éxito"))
                .response(responseBuilder().responseCode("400").description("Error en la validación de los datos"));
    }

    private Consumer<Builder> docGetAllBootcamps() {
        return ops -> ops.tag(TAG_BOOTCAMP)
                .operationId("getAllBootcamps")
                .summary("Listar todos los Bootcamps")
                .description("Retorna una lista paginada o completa de los programas disponibles.")
                .response(responseBuilder().responseCode("200").description("Lista obtenida correctamente"));
    }

    private Consumer<Builder> docGetBootcampById() {
        return ops -> ops.tag(TAG_BOOTCAMP)
                .operationId("getBootcampById")
                .summary("Consultar Bootcamp por ID")
                .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("id")
                        .description("ID del Bootcamp")
                        .example("550e8400-e29b-41d4-a716-446655440000"))
                .response(responseBuilder().responseCode("200").description("Bootcamp encontrado"))
                .response(responseBuilder().responseCode("404").description("Bootcamp no existe"));
    }

    private Consumer<Builder> docGetBootcampCompleto() {
        return ops -> ops.tag(TAG_BOOTCAMP)
                .operationId("getBootcampCompleto")
                .summary("Obtener información completa del Bootcamp (HU8)")
                .description("Retorna toda la información del bootcamp incluyendo personas inscritas (nombre y correo), capacidades y tecnologías asociadas.")
                .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("id")
                        .description("ID del Bootcamp")
                        .example("1"))
                .response(responseBuilder().responseCode("200").description("Información completa del bootcamp"))
                .response(responseBuilder().responseCode("404").description("Bootcamp no encontrado"));
    }

    private Consumer<Builder> docDeleteBootcamp() {
        return ops -> ops.tag(TAG_BOOTCAMP)
                .operationId("deleteBootcamp")
                .summary("Eliminar Bootcamp por ID")
                .description("Elimina un bootcamp y sus capacidades/tecnologías asociadas si no están referenciadas por otros bootcamps.")
                .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("id")
                        .description("ID del Bootcamp a eliminar")
                        .example("1"))
                .response(responseBuilder().responseCode("204").description("Bootcamp eliminado exitosamente"))
                .response(responseBuilder().responseCode("404").description("Bootcamp no encontrado"));
    }
}
