# Script de Prueba Completa HU6 - Escenario Controlado
# Crea bootcamps específicos para demostrar eliminación cascade condicional

Write-Host "`n======================================================" -ForegroundColor Cyan
Write-Host "HU6 - PRUEBA COMPLETA CON ESCENARIO CONTROLADO" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan

$baseUrlBootcamp = "http://localhost:8083/api/bootcamp"
$baseUrlCapacidad = "http://localhost:8082/api/capacidad"
$baseUrlTecnologia = "http://localhost:8081/api/tecnologia"

function Invoke-SafeRequest {
    param(
        [string]$Uri,
        [string]$Method = "Get",
        [object]$Body = $null
    )
    try {
        if ($Body) {
            return Invoke-RestMethod -Uri $Uri -Method $Method -Body ($Body | ConvertTo-Json) -ContentType "application/json" -ErrorAction Stop
        } else {
            return Invoke-RestMethod -Uri $Uri -Method $Method -ErrorAction Stop
        }
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 404) {
            return $null
        }
        Write-Host "Error en request: $($_.Exception.Message)" -ForegroundColor DarkRed
        return $null
    }
}

Write-Host "`n=== ESCENARIO DE PRUEBA ===" -ForegroundColor Yellow
Write-Host "Se crearán 3 bootcamps:" -ForegroundColor White
Write-Host "  - Bootcamp Alpha: capacidades [1, 2]" -ForegroundColor Gray
Write-Host "  - Bootcamp Beta:  capacidades [2, 3]" -ForegroundColor Gray
Write-Host "  - Bootcamp Gamma: capacidades [5]" -ForegroundColor Gray
Write-Host "`nSe eliminará Bootcamp Alpha:" -ForegroundColor White
Write-Host "  - Capacidad 1 debe eliminarse (solo en Alpha)" -ForegroundColor Red
Write-Host "  - Capacidad 2 debe conservarse (compartida con Beta)" -ForegroundColor Green
Write-Host "`nSe eliminará Bootcamp Gamma:" -ForegroundColor White
Write-Host "  - Capacidad 5 debe eliminarse (solo en Gamma)" -ForegroundColor Red
Write-Host "  - Las tecnologías de capacidad 5 se evalúan" -ForegroundColor Yellow

Write-Host "`n=== PASO 1: Crear Bootcamp Alpha ===" -ForegroundColor Yellow
$bodyAlpha = @{
    nombre = "Bootcamp Alpha HU6"
    descripcion = "Bootcamp de prueba A"
    fechaLanzamiento = "2026-03-01"
    duracionSemanas = 8
    capacidadesIds = @(1, 2)
}

$bootcampAlpha = Invoke-SafeRequest -Uri $baseUrlBootcamp -Method Post -Body $bodyAlpha

if ($null -ne $bootcampAlpha) {
    Write-Host "✓ Bootcamp Alpha creado con ID: $($bootcampAlpha.id)" -ForegroundColor Green
    $alphaId = $bootcampAlpha.id
} else {
    Write-Host "✗ Error al crear Bootcamp Alpha" -ForegroundColor Red
    exit
}

Write-Host "`n=== PASO 2: Crear Bootcamp Beta ===" -ForegroundColor Yellow
$bodyBeta = @{
    nombre = "Bootcamp Beta HU6"
    descripcion = "Bootcamp de prueba B"
    fechaLanzamiento = "2026-03-15"
    duracionSemanas = 10
    capacidadesIds = @(2, 3)
}

$bootcampBeta = Invoke-SafeRequest -Uri $baseUrlBootcamp -Method Post -Body $bodyBeta

if ($null -ne $bootcampBeta) {
    Write-Host "✓ Bootcamp Beta creado con ID: $($bootcampBeta.id)" -ForegroundColor Green
    $betaId = $bootcampBeta.id
} else {
    Write-Host "✗ Error al crear Bootcamp Beta" -ForegroundColor Red
    exit
}

Write-Host "`n=== PASO 3: Crear Bootcamp Gamma ===" -ForegroundColor Yellow
$bodyGamma = @{
    nombre = "Bootcamp Gamma HU6"
    descripcion = "Bootcamp de prueba C con capacidad única"
    fechaLanzamiento = "2026-04-01"
    duracionSemanas = 6
    capacidadesIds = @(5)
}

$bootcampGamma = Invoke-SafeRequest -Uri $baseUrlBootcamp -Method Post -Body $bodyGamma

if ($null -ne $bootcampGamma) {
    Write-Host "✓ Bootcamp Gamma creado con ID: $($bootcampGamma.id)" -ForegroundColor Green
    $gammaId = $bootcampGamma.id
} else {
    Write-Host "✗ Error al crear Bootcamp Gamma" -ForegroundColor Red
    exit
}

Start-Sleep -Seconds 1

Write-Host "`n=== PASO 4: Verificar estado inicial ===" -ForegroundColor Yellow
$bootcampsCreados = Invoke-SafeRequest -Uri $baseUrlBootcamp

Write-Host "Total de bootcamps: $($bootcampsCreados.Count)" -ForegroundColor White
$alphaData = $bootcampsCreados | Where-Object { $_.id -eq $alphaId } | Select-Object -First 1
$betaData = $bootcampsCreados | Where-Object { $_.id -eq $betaId } | Select-Object -First 1
$gammaData = $bootcampsCreados | Where-Object { $_.id -eq $gammaId } | Select-Object -First 1

Write-Host "`nBootcamp Alpha (ID: $alphaId):" -ForegroundColor White
Write-Host "  Capacidades: $($alphaData.capacidades.Count)" -ForegroundColor Gray
foreach ($cap in $alphaData.capacidades) {
    Write-Host "    - ID: $($cap.id), Nombre: $($cap.nombre), Tecnologías: $($cap.tecnologias.Count)" -ForegroundColor DarkGray
}

Write-Host "`nBootcamp Beta (ID: $betaId):" -ForegroundColor White
Write-Host "  Capacidades: $($betaData.capacidades.Count)" -ForegroundColor Gray
foreach ($cap in $betaData.capacidades) {
    Write-Host "    - ID: $($cap.id), Nombre: $($cap.nombre), Tecnologías: $($cap.tecnologias.Count)" -ForegroundColor DarkGray
}

Write-Host "`nBootcamp Gamma (ID: $gammaId):" -ForegroundColor White
Write-Host "  Capacidades: $($gammaData.capacidades.Count)" -ForegroundColor Gray
foreach ($cap in $gammaData.capacidades) {
    Write-Host "    - ID: $($cap.id), Nombre: $($cap.nombre), Tecnologías: $($cap.tecnologias.Count)" -ForegroundColor DarkGray
}

Write-Host "`n=== PASO 5: Eliminar Bootcamp Alpha ===" -ForegroundColor Yellow
Write-Host "Eliminando bootcamp con ID: $alphaId" -ForegroundColor White

try {
    Invoke-RestMethod -Uri "$baseUrlBootcamp/$alphaId" -Method Delete -ErrorAction Stop | Out-Null
    Write-Host "✓ DELETE ejecutado exitosamente" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 204) {
        Write-Host "✓ DELETE ejecutado exitosamente (204 No Content)" -ForegroundColor Green
    } else {
        Write-Host "✗ Error al eliminar: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Start-Sleep -Seconds 2

Write-Host "`n=== PASO 6: Verificar eliminación de Bootcamp Alpha ===" -ForegroundColor Yellow
$alphaVerificacion = Invoke-SafeRequest -Uri "$baseUrlBootcamp/$alphaId"

if ($null -eq $alphaVerificacion) {
    Write-Host "✓ Bootcamp Alpha eliminado correctamente" -ForegroundColor Green
} else {
    Write-Host "✗ ERROR: Bootcamp Alpha aún existe" -ForegroundColor Red
}

Write-Host "`n=== PASO 7: Verificar capacidades después de eliminar Alpha ===" -ForegroundColor Yellow

Write-Host "`nCapacidad 1 (solo en Alpha - DEBE eliminarse):" -ForegroundColor White
$cap1 = Invoke-SafeRequest -Uri "$baseUrlCapacidad/1"
if ($null -eq $cap1) {
    Write-Host "  ✓ Capacidad 1 ELIMINADA correctamente" -ForegroundColor Green
} else {
    Write-Host "  ✗ ERROR: Capacidad 1 aún existe" -ForegroundColor Red
}

Write-Host "`nCapacidad 2 (compartida Alpha-Beta - NO debe eliminarse):" -ForegroundColor White
$cap2 = Invoke-SafeRequest -Uri "$baseUrlCapacidad/2"
if ($null -ne $cap2) {
    Write-Host "  ✓ Capacidad 2 CONSERVADA correctamente" -ForegroundColor Green
    Write-Host "    Nombre: $($cap2.nombre)" -ForegroundColor Gray
} else {
    Write-Host "  ✗ ERROR: Capacidad 2 fue eliminada incorrectamente" -ForegroundColor Red
}

Write-Host "`nCapacidad 3 (solo en Beta - NO debe eliminarse):" -ForegroundColor White
$cap3 = Invoke-SafeRequest -Uri "$baseUrlCapacidad/3"
if ($null -ne $cap3) {
    Write-Host "  ✓ Capacidad 3 CONSERVADA correctamente" -ForegroundColor Green
} else {
    Write-Host "  ✗ ERROR: Capacidad 3 fue eliminada" -ForegroundColor Red
}

Write-Host "`n=== PASO 8: Verificar Bootcamp Beta ===" -ForegroundColor Yellow
$betaVerificacion = Invoke-SafeRequest -Uri "$baseUrlBootcamp/$betaId"

if ($null -ne $betaVerificacion) {
    Write-Host "✓ Bootcamp Beta sigue existiendo" -ForegroundColor Green
    Write-Host "  Capacidades: $($betaVerificacion.capacidades.Count)" -ForegroundColor Gray
    $cap2EnBeta = $betaVerificacion.capacidades | Where-Object { $_.id -eq 2 }
    if ($null -ne $cap2EnBeta) {
        Write-Host "  ✓ Capacidad 2 presente en Beta" -ForegroundColor Green
    }
} else {
    Write-Host "✗ ERROR: Bootcamp Beta fue eliminado" -ForegroundColor Red
}

Write-Host "`n=== PASO 9: Eliminar Bootcamp Gamma ===" -ForegroundColor Yellow
Write-Host "Eliminando bootcamp con ID: $gammaId" -ForegroundColor White

try {
    Invoke-RestMethod -Uri "$baseUrlBootcamp/$gammaId" -Method Delete -ErrorAction Stop | Out-Null
    Write-Host "✓ DELETE ejecutado exitosamente" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 204) {
        Write-Host "✓ DELETE ejecutado exitosamente (204 No Content)" -ForegroundColor Green
    } else {
        Write-Host "✗ Error al eliminar: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Start-Sleep -Seconds 2

Write-Host "`n=== PASO 10: Verificar eliminación de Bootcamp Gamma ===" -ForegroundColor Yellow
$gammaVerificacion = Invoke-SafeRequest -Uri "$baseUrlBootcamp/$gammaId"

if ($null -eq $gammaVerificacion) {
    Write-Host "✓ Bootcamp Gamma eliminado correctamente" -ForegroundColor Green
} else {
    Write-Host "✗ ERROR: Bootcamp Gamma aún existe" -ForegroundColor Red
}

Write-Host "`nCapacidad 5 (solo en Gamma - DEBE eliminarse):" -ForegroundColor White
$cap5 = Invoke-SafeRequest -Uri "$baseUrlCapacidad/5"
if ($null -eq $cap5) {
    Write-Host "  ✓ Capacidad 5 ELIMINADA correctamente" -ForegroundColor Green
} else {
    Write-Host "  ✗ ERROR: Capacidad 5 aún existe" -ForegroundColor Red
}

Write-Host "`n======================================================" -ForegroundColor Cyan
Write-Host "RESUMEN FINAL HU6" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan

$pruebasExitosas = 0
$totalPruebas = 6

# Prueba 1: Alpha eliminado
if ($null -eq $alphaVerificacion) {
    Write-Host "✓ 1. Bootcamp Alpha eliminado" -ForegroundColor Green
    $pruebasExitosas++
} else {
    Write-Host "✗ 1. Bootcamp Alpha NO eliminado" -ForegroundColor Red
}

# Prueba 2: Capacidad 1 eliminada
if ($null -eq $cap1) {
    Write-Host "✓ 2. Capacidad 1 eliminada (única en Alpha)" -ForegroundColor Green
    $pruebasExitosas++
} else {
    Write-Host "✗ 2. Capacidad 1 NO eliminada" -ForegroundColor Red
}

# Prueba 3: Capacidad 2 conservada
if ($null -ne $cap2) {
    Write-Host "✓ 3. Capacidad 2 conservada (compartida)" -ForegroundColor Green
    $pruebasExitosas++
} else {
    Write-Host "✗ 3. Capacidad 2 eliminada incorrectamente" -ForegroundColor Red
}

# Prueba 4: Beta conservado
if ($null -ne $betaVerificacion) {
    Write-Host "✓ 4. Bootcamp Beta conservado" -ForegroundColor Green
    $pruebasExitosas++
} else {
    Write-Host "✗ 4. Bootcamp Beta eliminado incorrectamente" -ForegroundColor Red
}

# Prueba 5: Gamma eliminado
if ($null -eq $gammaVerificacion) {
    Write-Host "✓ 5. Bootcamp Gamma eliminado" -ForegroundColor Green
    $pruebasExitosas++
} else {
    Write-Host "✗ 5. Bootcamp Gamma NO eliminado" -ForegroundColor Red
}

# Prueba 6: Capacidad 5 eliminada
if ($null -eq $cap5) {
    Write-Host "✓ 6. Capacidad 5 eliminada (única en Gamma)" -ForegroundColor Green
    $pruebasExitosas++
} else {
    Write-Host "✗ 6. Capacidad 5 NO eliminada" -ForegroundColor Red
}

Write-Host "`n======================================================" -ForegroundColor Cyan
Write-Host "Resultado Final: $pruebasExitosas/$totalPruebas pruebas exitosas" -ForegroundColor $(if ($pruebasExitosas -eq $totalPruebas) { "Green" } else { "Yellow" })
Write-Host "======================================================" -ForegroundColor Cyan

if ($pruebasExitosas -eq $totalPruebas) {
    Write-Host "`n¡HU6 IMPLEMENTADA CORRECTAMENTE! ✓" -ForegroundColor Green
} else {
    Write-Host "`nAlgunas pruebas fallaron. Revisar implementación." -ForegroundColor Yellow
}
