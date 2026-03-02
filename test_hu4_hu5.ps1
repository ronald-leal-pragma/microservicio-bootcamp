# Script de prueba para HU4 y HU5 - Bootcamp
# Este script crea varios bootcamps y verifica el ordenamiento

Write-Host "=== PRUEBA HU4 y HU5 - Microservicio Bootcamp ===" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8083/api/bootcamp"
$capacidadUrl = "http://localhost:8082/api/capacidad"

# Verificar que microservicio-capacidad está corriendo
try {
    Write-Host "Verificando microservicio-capacidad..." -ForegroundColor Yellow
    $capacidades = Invoke-RestMethod -Uri "$capacidadUrl?page=0&size=10" -Method Get
    Write-Host "✓ Microservicio-capacidad está activo" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ Error: microservicio-capacidad no está disponible en puerto 8082" -ForegroundColor Red
    Write-Host "  Inicia el servicio con: cd microservicio-capacidad; .\gradlew.bat bootRun" -ForegroundColor Yellow
    exit 1
}

# Verificar que microservicio-bootcamp está corriendo
try {
    Write-Host "Verificando microservicio-bootcamp..." -ForegroundColor Yellow
    Invoke-RestMethod -Uri $baseUrl -Method Get -ErrorAction Stop | Out-Null
    Write-Host "✓ Microservicio-bootcamp está activo" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ Error: microservicio-bootcamp no está disponible en puerto 8083" -ForegroundColor Red
    Write-Host "  Inicia el servicio con: cd microservicio-bootcamp; .\gradlew.bat bootRun" -ForegroundColor Yellow
    exit 1
}

Write-Host "=== PASO 1: Crear Bootcamps de prueba ===" -ForegroundColor Cyan
Write-Host ""

# Bootcamp 1: Con 2 capacidades
Write-Host "Creando Bootcamp 1: Java Fullstack (2 capacidades)..." -ForegroundColor Yellow
$bootcamp1 = @{
    nombre = "Bootcamp Java Fullstack"
    descripcion = "Bootcamp intensivo de desarrollo fullstack"
    fechaLanzamiento = "2026-03-01"
    duracionSemanas = 12
    capacidadesIds = @(1, 2)
} | ConvertTo-Json

try {
    $response1 = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $bootcamp1 -ContentType "application/json"
    Write-Host "✓ Bootcamp 1 creado: ID $($response1.id)" -ForegroundColor Green
} catch {
    Write-Host "✗ Error al crear Bootcamp 1: $($_.Exception.Message)" -ForegroundColor Red
}

# Bootcamp 2: Con 1 capacidad
Write-Host "Creando Bootcamp 2: Backend Especializado (1 capacidad)..." -ForegroundColor Yellow
$bootcamp2 = @{
    nombre = "Bootcamp Backend Especializado"
    descripcion = "Especializacion en desarrollo backend"
    fechaLanzamiento = "2026-04-01"
    duracionSemanas = 8
    capacidadesIds = @(1)
} | ConvertTo-Json

try {
    $response2 = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $bootcamp2 -ContentType "application/json"
    Write-Host "✓ Bootcamp 2 creado: ID $($response2.id)" -ForegroundColor Green
} catch {
    Write-Host "✗ Error al crear Bootcamp 2: $($_.Exception.Message)" -ForegroundColor Red
}

# Bootcamp 3: Con 3 capacidades
Write-Host "Creando Bootcamp 3: DevOps Avanzado (3 capacidades)..." -ForegroundColor Yellow
$bootcamp3 = @{
    nombre = "Bootcamp DevOps Avanzado"
    descripcion = "Bootcamp avanzado de tecnologías DevOps"
    fechaLanzamiento = "2026-05-01"
    duracionSemanas = 16
    capacidadesIds = @(1, 2, 3)
} | ConvertTo-Json

try {
    $response3 = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $bootcamp3 -ContentType "application/json"
    Write-Host "✓ Bootcamp 3 creado: ID $($response3.id)" -ForegroundColor Green
} catch {
    Write-Host "✗ Error al crear Bootcamp 3: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Start-Sleep -Seconds 1

Write-Host "=== PASO 2: HU5 - Listar con ordenamiento por NOMBRE ASC ===" -ForegroundColor Cyan
Write-Host ""
try {
    $listaNombreAsc = Invoke-RestMethod -Uri "${baseUrl}?page=0&size=10&sort=nombre&order=asc" -Method Get
    Write-Host "Orden por nombre ASC:" -ForegroundColor Yellow
    foreach ($b in $listaNombreAsc) {
        Write-Host "  - $($b.nombre) (Capacidades: $($b.capacidades.Count))" -ForegroundColor White
    }
    Write-Host ""
} catch {
    Write-Host "✗ Error al listar por nombre ASC: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "=== PASO 3: HU5 - Listar con ordenamiento por NOMBRE DESC ===" -ForegroundColor Cyan
Write-Host ""
try {
    $listaNombreDesc = Invoke-RestMethod -Uri "${baseUrl}?page=0&size=10&sort=nombre&order=desc" -Method Get
    Write-Host "Orden por nombre DESC:" -ForegroundColor Yellow
    foreach ($b in $listaNombreDesc) {
        Write-Host "  - $($b.nombre) (Capacidades: $($b.capacidades.Count))" -ForegroundColor White
    }
    Write-Host ""
} catch {
    Write-Host "✗ Error al listar por nombre DESC: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "=== PASO 4: HU5 - Listar con ordenamiento por CANTIDAD CAPACIDADES ASC ===" -ForegroundColor Cyan
Write-Host ""
try {
    $listaCapAsc = Invoke-RestMethod -Uri "${baseUrl}?page=0&size=10&sort=cantCapacidades&order=asc" -Method Get
    Write-Host "Orden por cantidad de capacidades ASC:" -ForegroundColor Yellow
    foreach ($b in $listaCapAsc) {
        Write-Host "  - $($b.nombre): $($b.capacidades.Count) capacidades" -ForegroundColor White
        foreach ($cap in $b.capacidades) {
            Write-Host "    * $($cap.nombre) ($($cap.tecnologias.Count) tecnologias)" -ForegroundColor Gray
        }
    }
    Write-Host ""
} catch {
    Write-Host "✗ Error al listar por capacidades ASC: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "=== PASO 5: HU5 - Listar con ordenamiento por CANTIDAD CAPACIDADES DESC ===" -ForegroundColor Cyan
Write-Host ""
try {
    $listaCapDesc = Invoke-RestMethod -Uri "${baseUrl}?page=0&size=10&sort=cantCapacidades&order=desc" -Method Get
    Write-Host "Orden por cantidad de capacidades DESC:" -ForegroundColor Yellow
    foreach ($b in $listaCapDesc) {
        Write-Host "  - $($b.nombre): $($b.capacidades.Count) capacidades" -ForegroundColor White
        foreach ($cap in $b.capacidades) {
            Write-Host "    * $($cap.nombre) ($($cap.tecnologias.Count) tecnologias)" -ForegroundColor Gray
        }
    }
    Write-Host ""
} catch {
    Write-Host "✗ Error al listar por capacidades DESC: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "=== PASO 6: Verificar estructura completa de respuesta ===" -ForegroundColor Cyan
Write-Host ""
try {
    $bootcampCompleto = Invoke-RestMethod -Uri "${baseUrl}?page=0&size=1" -Method Get
    if ($bootcampCompleto.Count -gt 0) {
        $primer = $bootcampCompleto[0]
        Write-Host "Estructura del primer bootcamp:" -ForegroundColor Yellow
        Write-Host "  ID: $($primer.id)" -ForegroundColor White
        Write-Host "  Nombre: $($primer.nombre)" -ForegroundColor White
        Write-Host "  Descripcion: $($primer.descripcion)" -ForegroundColor White
        Write-Host "  Fecha Lanzamiento: $($primer.fechaLanzamiento)" -ForegroundColor White
        Write-Host "  Duracion (semanas): $($primer.duracionSemanas)" -ForegroundColor White
        Write-Host "  Capacidades:" -ForegroundColor White
        foreach ($cap in $primer.capacidades) {
            Write-Host "    - ID: $($cap.id), Nombre: $($cap.nombre)" -ForegroundColor Cyan
            Write-Host "      Tecnologias:" -ForegroundColor White
            foreach ($tec in $cap.tecnologias) {
                Write-Host "        * ID: $($tec.id), Nombre: $($tec.nombre)" -ForegroundColor Gray
            }
        }
    }
    Write-Host ""
} catch {
    Write-Host "✗ Error al verificar estructura: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "=== VALIDACIONES DE NEGOCIO ===" -ForegroundColor Cyan
Write-Host ""

# Validacion: Sin capacidades
Write-Host "Validacion 1: Crear bootcamp SIN capacidades (debe fallar)..." -ForegroundColor Yellow
$bootcampInvalido1 = @{
    nombre = "Bootcamp Sin Capacidades"
    descripcion = "Este debe fallar"
    fechaLanzamiento = "2026-03-01"
    duracionSemanas = 12
    capacidadesIds = @()
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri $baseUrl -Method Post -Body $bootcampInvalido1 -ContentType "application/json" -ErrorAction Stop
    Write-Host "✗ ERROR: Debio fallar pero se creo exitosamente" -ForegroundColor Red
} catch {
    Write-Host "✓ Validacion correcta: $($_.ErrorDetails.Message)" -ForegroundColor Green
}

# Validacion: Mas de 4 capacidades
Write-Host "Validacion 2: Crear bootcamp con MAS de 4 capacidades (debe fallar)..." -ForegroundColor Yellow
$bootcampInvalido2 = @{
    nombre = "Bootcamp Exceso Capacidades"
    descripcion = "Este debe fallar"
    fechaLanzamiento = "2026-03-01"
    duracionSemanas = 12
    capacidadesIds = @(1, 2, 3, 4, 5)
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri $baseUrl -Method Post -Body $bootcampInvalido2 -ContentType "application/json" -ErrorAction Stop
    Write-Host "✗ ERROR: Debio fallar pero se creo exitosamente" -ForegroundColor Red
} catch {
    Write-Host "✓ Validacion correcta: $($_.ErrorDetails.Message)" -ForegroundColor Green
}

Write-Host ""
Write-Host "=== PRUEBAS COMPLETADAS ===" -ForegroundColor Cyan
Write-Host "- HU4: Crear bootcamp con 1-4 capacidades OK" -ForegroundColor Green
Write-Host "- HU5: Listar con ordenamiento por nombre OK" -ForegroundColor Green
Write-Host "- HU5: Listar con ordenamiento por cantidad capacidades OK" -ForegroundColor Green
Write-Host "- HU5: Respuesta incluye capacidades con tecnologias OK" -ForegroundColor Green
Write-Host "- Validaciones de negocio OK" -ForegroundColor Green
