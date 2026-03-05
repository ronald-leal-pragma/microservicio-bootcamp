package com.pragma.bootcamp.infrastructure.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;
@Getter
@Setter
@Document(collection = "bootcamp_reportes")
public class BootcampReporteMongoEntity {

    @Id
    private String id;

    @Field("bootcamp_id")
    private String bootcampId;

    @Field("bootcamp_nombre")
    private String bootcampNombre;

    @Field("bootcamp_descripcion")
    private String bootcampDescripcion;

    @Field("cantidad_personas_inscritas")
    private int cantidadPersonasInscritas;

    @Field("cantidad_capacidades")
    private int cantidadCapacidades;

    @Field("cantidad_tecnologias")
    private int cantidadTecnologias;

    @Field("metricas")
    private Map<String, Object> metricas;

    @Field("fecha_generacion")
    private LocalDateTime fechaGeneracion;

}
