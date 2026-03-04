-- =========================================================
-- MICROSERVICIO BOOTCAMP - SCHEMA
-- =========================================================
-- Base de datos: bootcamp_db
-- Puerto: 8083
-- =========================================================

-- Tabla principal de Bootcamps
CREATE TABLE IF NOT EXISTS bootcamp (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(250) NOT NULL,
    fecha_lanzamiento DATE NOT NULL,
    duracion_semanas INT NOT NULL,
    capacidades_ids JSON NOT NULL,
    INDEX idx_bootcamp_nombre (nombre)
);

-- Tabla de relación Bootcamp-Capacidad (opcional si se usa JSON)
CREATE TABLE IF NOT EXISTS bootcamp_capacidad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bootcamp_id BIGINT NOT NULL,
    capacidad_id BIGINT NOT NULL,
    FOREIGN KEY (bootcamp_id) REFERENCES bootcamp(id) ON DELETE CASCADE,
    UNIQUE KEY uk_bootcamp_capacidad (bootcamp_id, capacidad_id)
);

-- Tabla de Inscripciones
-- NOTA: persona_id referencia a personas en microservicio-persona (puerto 8084)
-- No hay FK porque las personas están en otra BD (persona_db)
CREATE TABLE IF NOT EXISTS inscripcion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    persona_id BIGINT NOT NULL,
    bootcamp_id BIGINT NOT NULL,
    fecha_inscripcion DATE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    FOREIGN KEY (bootcamp_id) REFERENCES bootcamp(id) ON DELETE CASCADE,
    INDEX idx_inscripcion_persona (persona_id),
    INDEX idx_inscripcion_bootcamp (bootcamp_id),
    UNIQUE KEY uk_persona_bootcamp (persona_id, bootcamp_id)
);
