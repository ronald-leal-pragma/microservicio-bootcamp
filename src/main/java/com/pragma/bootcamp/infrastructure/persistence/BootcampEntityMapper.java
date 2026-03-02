package com.pragma.bootcamp.infrastructure.persistence;

import com.pragma.bootcamp.domain.models.Bootcamp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.HashSet;

@Component
public class BootcampEntityMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Bootcamp toDomain(BootcampEntity entity) {
        if (entity == null) {
            return null;
        }

        Bootcamp bootcamp = new Bootcamp();
        bootcamp.setId(entity.getId());
        bootcamp.setNombre(entity.getNombre());
        bootcamp.setDescripcion(entity.getDescripcion());
        bootcamp.setFechaLanzamiento(entity.getFechaLanzamiento());
        bootcamp.setDuracionSemanas(entity.getDuracionSemanas());

        if (entity.getCapacidadesIds() != null && !entity.getCapacidadesIds().trim().isEmpty()) {
            bootcamp.setCapacidadesIds(jsonToSet(entity.getCapacidadesIds()));
        }

        return bootcamp;
    }

    public BootcampEntity toEntity(Bootcamp domain) {
        if (domain == null) {
            return null;
        }

        BootcampEntity entity = new BootcampEntity();
        entity.setId(domain.getId());
        entity.setNombre(domain.getNombre());
        entity.setDescripcion(domain.getDescripcion());
        entity.setFechaLanzamiento(domain.getFechaLanzamiento());
        entity.setDuracionSemanas(domain.getDuracionSemanas());

        if (domain.getCapacidadesIds() != null && !domain.getCapacidadesIds().isEmpty()) {
            entity.setCapacidadesIds(setToJson(domain.getCapacidadesIds()));
        }

        return entity;
    }

    private String setToJson(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al convertir Set a JSON", e);
        }
    }

    private Set<Long> jsonToSet(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("[]")) {
            return new HashSet<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Set<Long>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al convertir JSON a Set", e);
        }
    }
}
