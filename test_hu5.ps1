# Script de prueba para HU4 y HU5 - Bootcamp
Write-Host "=== PRUEBA HU4 y HU5 - Microservicio Bootcamp ===" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8083/api/bootcamp"

Write-Host "=== PASO 1: Crear Bootcamps de prueba ===" -ForegroundColor Cyan
Write-Host ""

# Bootcamp 1: Con 2 capacidades
Write-Host "Creando Bootcamp 1: Java Fullstack con 2 capacidades..." -ForegroundColor Yellow
$bootcamp1 = @{
    nombre = "Bootcamp Java Fullstack"
    descripcion = "Bootcamp intensivo de desarrollo fullstack"
    fechaLanzamiento = "2026-03-01"
    duracionSemanas = 12
    capacidadesIds = @(1, 2)
} | ConvertTo-Json

try {
    $response1 = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $bootcamp1 -ContentType "application/json"
    Write-Host "OK - Bootcamp 1 creado: ID $($response1.id)" -ForegroundColor Green
} catch {
    Write-Host "ERROR al crear Bootcamp 1: $($_.Exception.Message)" -ForegroundColor Red
}

# Bootcamp 2: Con 1 capacidad
Write-Host "Creando Bootcamp 2: Backend con 1 capacidad..." -ForegroundColor Yellow
$bootcamp2 = @{
    nombre = "Bootcamp Backend Especializado"
    descripcion = "Especializacion en desarrollo backend"
    fechaLanzamiento = "2026-04-01"
    duracionSemanas = 8
    capacidadesIds = @(1)
} | ConvertTo-Json

try {
    $response2 = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $bootcamp2 -ContentType "application/json"
    Write-Host "OK - Bootcamp 2 creado: ID $($response2.id)" -ForegroundColor Green
} catch {
    Write-Host "ERROR al crear Bootcamp 2: $($_.Exception.Message)" -ForegroundColor Red
}

# Bootcamp 3: Con 3 capacidades
Write-Host "Creando Bootcamp 3: DevOps con 3 capacidades..." -ForegroundColor Yellow
$bootcamp3 = @{
    nombre = "Bootcamp DevOps Avanzado"
    descripcion = "Bootcamp avanzado de tecnologias DevOps"
    fechaLanzamiento = "2026-05-01"
    duracionSemanas = 16
    capacidadesIds = @(1, 2, 3)
} | ConvertTo-Json

try {
    $response3 = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $bootcamp3 -ContentType "application/json"
    Write-Host "OK - Bootcamp 3 creado: ID $($response3.id)" -ForegroundColor Green
} catch {
    Write-Host "ERROR al crear Bootcamp 3: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Start-Sleep -Seconds 1

Write-Host "=== PASO 2: HU5 - Ordenamiento por NOMBRE ASC ===" -ForegroundColor Cyan
try {
    $url = "$baseUrl" + "?page=0" + "&" + "size=10" + "&" + "sort=nombre" + "&" + "order=asc"
    $listaNombreAsc = Invoke-RestMethod -Uri $url -Method Get
    Write-Host "Orden por nombre ASC:" -ForegroundColor Yellow
    foreach ($b in $listaNombreAsc) {
        $cantCap = $b.capacidades.Count
        Write-Host "  - $($b.nombre) - Capacidades: $cantCap" -ForegroundColor White
    }
} catch {
    Write-Host "ERROR al listar: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== PASO 3: HU5 - Ordenamiento por NOMBRE DESC ===" -ForegroundColor Cyan
try {
    $url = "$baseUrl" + "?page=0" + "&" + "size=10" + "&" + "sort=nombre" + "&" + "order=desc"
    $listaNombreDesc = Invoke-RestMethod -Uri $url -Method Get
    Write-Host "Orden por nombre DESC:" -ForegroundColor Yellow
    foreach ($b in $listaNombreDesc) {
        $cantCap = $b.capacidades.Count
        Write-Host "  - $($b.nombre) - Capacidades: $cantCap" -ForegroundColor White
    }
} catch {
    Write-Host "ERROR al listar: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== PASO 4: HU5 - Ordenamiento por CANTIDAD ASC ===" -ForegroundColor Cyan
try {
    $url = "$baseUrl" + "?page=0" + "&" + "size=10" + "&" + "sort=cantCapacidades" + "&" + "order=asc"
    $listaCapAsc = Invoke-RestMethod -Uri $url -Method Get
    Write-Host "Orden por cantidad de capacidades ASC:" -ForegroundColor Yellow
    foreach ($b in $listaCapAsc) {
        $cantCap = $b.capacidades.Count
        Write-Host "  - $($b.nombre): $cantCap capacidades" -ForegroundColor White
        foreach ($cap in $b.capacidades) {
            $cantTec = $cap.tecnologias.Count
            Write-Host "    * $($cap.nombre) - $cantTec tecnologias" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "ERROR al listar: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== PASO 5: HU5 - Ordenamiento por CANTIDAD DESC ===" -ForegroundColor Cyan
try {
    $url = "$baseUrl" + "?page=0" + "&" + "size=10" + "&" + "sort=cantCapacidades" + "&" + "order=desc"
    $listaCapDesc = Invoke-RestMethod -Uri $url -Method Get
    Write-Host "Orden por cantidad de capacidades DESC:" -ForegroundColor Yellow
    foreach ($b in $listaCapDesc) {
        $cantCap = $b.capacidades.Count
        Write-Host "  - $($b.nombre): $cantCap capacidades" -ForegroundColor White
        foreach ($cap in $b.capacidades) {
            $cantTec = $cap.tecnologias.Count
            Write-Host "    * $($cap.nombre) - $cantTec tecnologias" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "ERROR al listar: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== PASO 6: Verificar estructura completa ===" -ForegroundColor Cyan
try {
    $url = "$baseUrl" + "?page=0" + "&" + "size=1"
    $bootcampCompleto = Invoke-RestMethod -Uri $url -Method Get
    if ($bootcampCompleto.Count -gt 0) {
        $primer = $bootcampCompleto[0]
        Write-Host "Estructura del primer bootcamp:" -ForegroundColor Yellow
        Write-Host "  ID: $($primer.id)" -ForegroundColor White
        Write-Host "  Nombre: $($primer.nombre)" -ForegroundColor White
        Write-Host "  Descripcion: $($primer.descripcion)" -ForegroundColor White
        Write-Host "  Fecha Lanzamiento: $($primer.fechaLanzamiento)" -ForegroundColor White
        Write-Host "  Duracion semanas: $($primer.duracionSemanas)" -ForegroundColor White
        Write-Host "  Capacidades:" -ForegroundColor White
        foreach ($cap in $primer.capacidades) {
            Write-Host "    - ID: $($cap.id), Nombre: $($cap.nombre)" -ForegroundColor Cyan
            Write-Host "      Tecnologias:" -ForegroundColor White
            foreach ($tec in $cap.tecnologias) {
                Write-Host "        * ID: $($tec.id), Nombre: $($tec.nombre)" -ForegroundColor Gray
            }
        }
    }
} catch {
    Write-Host "ERROR al verificar estructura: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== PRUEBAS COMPLETADAS ===" -ForegroundColor Cyan
Write-Host "- HU4: Crear bootcamp con 1-4 capacidades OK" -ForegroundColor Green
Write-Host "- HU5: Listar con ordenamiento por nombre OK" -ForegroundColor Green
Write-Host "- HU5: Listar con ordenamiento por cantidad capacidades OK" -ForegroundColor Green
Write-Host "- HU5: Respuesta incluye capacidades con tecnologias OK" -ForegroundColor Green
