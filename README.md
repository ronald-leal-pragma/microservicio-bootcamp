# Microservicio Bootcamp

## Descripción
Microservicio central para la gestión de bootcamps, personas e inscripciones. Un bootcamp agrupa capacidades y permite inscribir personas. Implementa arquitectura hexagonal con Spring WebFlux.

## Tecnologías
- Java 17
- Spring Boot 3.2.1
- Spring WebFlux (Programación Reactiva)
- R2DBC MySQL
- Gradle 8.5
- Lombok
- MapStruct

## Puerto
**8083**

## API Endpoints

### Bootcamps
- `POST /api/bootcamp` - Crear nuevo bootcamp
- `GET /api/bootcamp` - Listar todos los bootcamps (paginado)
- `GET /api/bootcamp/{id}` - Obtener bootcamp por ID
- `GET /api/bootcamp/{id}/completo` - Obtener bootcamp con detalle completo de capacidades y tecnologías
- `DELETE /api/bootcamp/{id}` - Eliminar bootcamp

### Personas
- `POST /api/persona` - Crear nueva persona
- `GET /api/persona/{id}` - Obtener persona por ID

### Inscripciones
- `POST /api/inscripcion` - Inscribir persona en un bootcamp
- `GET /api/inscripcion/{id}` - Obtener inscripción por ID
- `GET /api/inscripcion/persona/{personaId}` - Obtener todas las inscripciones de una persona

## Reglas de Negocio

### Bootcamps
- Debe tener entre 1 y 4 capacidades asociadas
- El nombre debe tener máximo 50 caracteres
- La descripción debe tener máximo 90 caracteres
- La duración debe estar entre 1 y 52 semanas

### Personas
- Email debe ser único en el sistema
- Número de documento debe ser único

### Inscripciones
- Una persona no puede tener más de 5 inscripciones activas simultáneamente
- No puede inscribirse en bootcamps con fechas superpuestas
- Validación automática de disponibilidad

## Ejecución

```bash
# Compilar
./gradlew clean build

# Ejecutar
./gradlew bootRun

# Ejecutar tests
./gradlew test

# Reporte de cobertura
./gradlew test jacocoTestReport
```

## Configuración Base de Datos
Editar `src/main/resources/application-local.yml`:

```yaml
spring:
  r2dbc:
    url: r2dbc:mysql://localhost:3306/bootcamp_db
    username: root
    password: tu_password
```

## Documentación API
Swagger UI disponible en: `http://localhost:8083/webjars/swagger-ui/index.html`

## Integración con Microservicios
- **Microservicio Capacidad** (puerto 8082): Obtiene información detallada de capacidades
- **Microservicio Tecnología** (puerto 8081): A través de Capacidad, obtiene información de tecnologías

## Cobertura de Tests
- **42 tests unitarios** configurados
- **90% de cobertura** en capa de UseCases
- Tests para: UseCases, Handlers, Adapters de Persistencia
- Configuración JaCoCo para reportes de cobertura
