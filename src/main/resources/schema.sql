CREATE TABLE IF NOT EXISTS bootcamp (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(250) NOT NULL,
    fecha_lanzamiento DATE NOT NULL,
    duracion_semanas INT NOT NULL,
    capacidades_ids JSON NOT NULL
);

CREATE TABLE IF NOT EXISTS bootcamp_capacidad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bootcamp_id BIGINT NOT NULL,
    capacidad_id BIGINT NOT NULL,
    FOREIGN KEY (bootcamp_id) REFERENCES bootcamp(id)
);

CREATE TABLE IF NOT EXISTS persona (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    documento VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS inscripcion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    persona_id BIGINT NOT NULL,
    bootcamp_id BIGINT NOT NULL,
    fecha_inscripcion DATE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    FOREIGN KEY (persona_id) REFERENCES persona(id),
    FOREIGN KEY (bootcamp_id) REFERENCES bootcamp(id)
);

CREATE TABLE IF NOT EXISTS bootcamp_reporte (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bootcamp_id BIGINT NOT NULL,
    bootcamp_nombre VARCHAR(100) NOT NULL,
    bootcamp_descripcion VARCHAR(250) NOT NULL,
    fecha_lanzamiento DATE NOT NULL,
    duracion_semanas INT NOT NULL,
    cantidad_capacidades INT NOT NULL,
    cantidad_tecnologias INT NOT NULL,
    cantidad_personas_inscritas INT NOT NULL DEFAULT 0,
    fecha_registro_reporte DATETIME NOT NULL,
    FOREIGN KEY (bootcamp_id) REFERENCES bootcamp(id) ON DELETE CASCADE,
    INDEX idx_bootcamp_id (bootcamp_id),
    INDEX idx_fecha_registro (fecha_registro_reporte)
);
