# Actualizaciones - Colección Postman Microservicios Bootcamp

**Archivo:** `Microservicios-Bootcamp.postman_collection.json`  
**Fecha:** 3 de marzo de 2026  
**Estado:** ✅ JSON válido

---

## 📋 Cambios Realizados

### 1. **HU1 - Tecnologías (Puerto 8081)**

#### ✅ Nuevo Endpoint: Filtrado por IDs
- **Request:** `Listar tecnologías filtradas por IDs`
- **Method:** GET
- **URL:** `http://localhost:8081/api/tecnologia?ids=1,2,4`
- **Query Params:**
  - `ids`: IDs separados por comas (ejemplo: `1,2,4`)
- **Descripción:** Permite obtener solo las tecnologías con los IDs especificados. Útil para validación batch desde otros microservicios (Capacidad, Bootcamp).
- **Respuesta:**
  ```json
  [
    { "id": 1, "nombre": "Java", "descripcion": "..." },
    { "id": 2, "nombre": "Spring Boot", "descripcion": "..." },
    { "id": 4, "nombre": "Docker", "descripcion": "..." }
  ]
  ```

---

### 2. **HU10 - Reportes (Puerto 8085)**

#### ✅ Actualizado: Listar Reportes con Metadata
- **Request:** `Listar Todos los Reportes (Paginado con Metadata)`
- **Method:** GET
- **URL:** `http://localhost:8085/api/reporte?page=0&size=10&sortBy=fechaGeneracion&sortOrder=desc`
- **Query Params:**
  - `page`: Número de página (base 0, default: 0)
  - `size`: Tamaño de la página (default: 10)
  - `sortBy`: Campo de ordenamiento (default: `fechaGeneracion`)
  - `sortOrder`: Dirección (asc/desc, default: `desc`)
- **Respuesta actualizada con PagedResponse:**
  ```json
  {
    "content": [
      {
        "id": "65e1...",
        "bootcampId": "1",
        "bootcampNombre": "Bootcamp Backend Avanzado",
        "cantidadPersonasInscritas": 15,
        "cantidadCapacidades": 2,
        "metricas": { ... },
        "fechaGeneracion": "2026-03-03T10:30:00"
      }
    ],
    "metadata": {
      "page": 0,
      "size": 10,
      "totalElements": 4,
      "totalPages": 1,
      "sortBy": "fechaGeneracion",
      "sortOrder": "desc"
    }
  }
  ```

#### ✅ Actualizados: Generar Reportes
- **Requests actualizados:**
  - `Generar Reporte de Bootcamp 1`
  - `Generar Reporte de Bootcamp 2`
  - `Generar Múltiples Reportes para un Bootcamp`

- **Body simplificado (antes tenía 9 campos, ahora 4):**
  ```json
  {
    "bootcampId": "1",
    "bootcampNombre": "Bootcamp Backend Avanzado",
    "cantidadPersonasInscritas": 15,
    "cantidadCapacidades": 2
  }
  ```

- **Campos removidos (ya no necesarios):**
  - ❌ `bootcampDescripcion`
  - ❌ `fechaLanzamiento`
  - ❌ `duracionSemanas`
  - ❌ `cantidadTecnologias`
  - ❌ `fechaRegistroReporte`

---

### 3. **Descripción General Actualizada**

Se actualizó la descripción de la colección para incluir:
- ✅ Paginación con metadata en todos los servicios
- ✅ Circuit Breaker (Resilience4j)
- ✅ Validación batch optimizada
- ✅ WebFlux reactivo
- ✅ MongoDB para reportes
- ✅ MySQL/R2DBC para otros servicios

---

## 📊 Resumen de Endpoints por Servicio

### Tecnología (8081)
| Endpoint | Method | Descripción |
|----------|--------|-------------|
| `/api/tecnologia` | POST | Crear tecnología |
| `/api/tecnologia` | GET | Listar todas |
| `/api/tecnologia?ids=1,2,3` | GET | **Filtrar por IDs** ⭐ |
| `/api/tecnologia/{id}` | GET | Obtener por ID |
| `/api/tecnologia/{id}` | DELETE | Eliminar |

### Capacidad (8082)
| Endpoint | Method | Descripción |
|----------|--------|-------------|
| `/api/capacidad` | POST | Crear capacidad |
| `/api/capacidad?page=0&size=10` | GET | Listar paginado con metadata |
| `/api/capacidad/{id}` | GET | Obtener por ID (incluye tecnologías) |
| `/api/capacidad/{id}` | DELETE | Eliminar |
| `/api/capacidad/validate-batch` | POST | Validar batch |

### Bootcamp (8083)
| Endpoint | Method | Descripción |
|----------|--------|-------------|
| `/api/bootcamp` | POST | Crear bootcamp |
| `/api/bootcamp?page=0&size=10` | GET | Listar paginado con metadata |
| `/api/bootcamp/{id}` | GET | Obtener por ID |
| `/api/bootcamp/{id}` | DELETE | Eliminar |

### Reporte (8085)
| Endpoint | Method | Descripción |
|----------|--------|-------------|
| `/api/reporte/generar` | POST | Generar reporte (simplificado) ⭐ |
| `/api/reporte?page=0&size=10&sortBy=fechaGeneracion&sortOrder=desc` | GET | **Listar con metadata** ⭐ |
| `/api/reporte/{id}` | GET | Obtener por ID |
| `/api/reporte/bootcamp/{id}` | GET | Listar por bootcamp |
| `/api/reporte/bootcamp/{id}/count` | GET | Contar reportes |

---

## 🔧 Configuración de MongoDB

Para que el microservicio de reportes funcione, asegúrate de haber configurado MongoDB:

**Credenciales necesarias:**
```properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=reporte_db
spring.data.mongodb.username=user_reporte
spring.data.mongodb.password=18566621
spring.data.mongodb.authentication-database=admin
```

**Pasos:**
1. Conectarse a MongoDB con usuario admin: `mongodb://admin:password123@localhost:27017/?authSource=admin`
2. Ejecutar el script: `setup-mongodb.js` (ver archivo en /microservicio-reporte/)
3. Verificar usuario creado en MongoDB Compass o mongosh

---

## ✅ Validación

El archivo JSON ha sido validado y es compatible con Postman Collection v2.1.0.

**Para importar:**
1. Abrir Postman
2. File → Import
3. Seleccionar: `Microservicios-Bootcamp.postman_collection.json`
4. Listo! Todos los endpoints actualizados

---

## 📝 Notas Importantes

1. **Filtrado de tecnologías:** El nuevo endpoint `?ids=1,2,3` reduce llamadas HTTP de N a 1
2. **Metadata de paginación:** Todos los endpoints "Listar" ahora retornan `PagedResponse` con metadata completo
3. **DTO simplificado:** Los reportes ahora solo requieren 4 campos en lugar de 9
4. **Validación batch:** Capacidad y Bootcamp usan el nuevo endpoint de filtrado
5. **Circuit Breaker:** Todos los servicios tienen Resilience4j configurado

---

**Total de requests en la colección:** ~50+ endpoints  
**Servicios cubiertos:** 5 microservicios  
**Health checks:** ✅ Incluidos para todos los servicios
