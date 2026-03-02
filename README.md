Microservicio Bootcamp

HU4: Un bootcamp tiene id, nombre, descripcion, fechaLanzamiento, duracionSemanas y un listado de capacidades asociadas (mínimo 1, máximo 4).

Puerto por defecto: 8083

Endpoints iniciales:
- POST /api/bootcamp  -> Crear bootcamp
- GET  /api/bootcamp  -> Listar bootcamps (paginado)
- GET  /api/bootcamp/{id} -> Obtener bootcamp por id

Sigue el patrón hexagonal usado en los otros microservicios.
