-- Script para configurar la base de datos y usuario de bootcamp
-- Ejecutar con: mysql -u root -p < setup_database.sql

CREATE DATABASE IF NOT EXISTS bootcamp_db;

CREATE USER IF NOT EXISTS 'user_boot'@'localhost' IDENTIFIED BY 'password';
CREATE USER IF NOT EXISTS 'user_boot'@'%' IDENTIFIED BY 'password';

GRANT ALL PRIVILEGES ON bootcamp_db.* TO 'user_boot'@'localhost';
GRANT ALL PRIVILEGES ON bootcamp_db.* TO 'user_boot'@'%';

FLUSH PRIVILEGES;

USE bootcamp_db;

-- Crear tabla bootcamp
CREATE TABLE IF NOT EXISTS bootcamp (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    fecha_lanzamiento DATE NOT NULL,
    duracion_semanas INT NOT NULL,
    capacidades_ids VARCHAR(500) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
