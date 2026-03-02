# Script de Prueba para HU6 - Eliminar Bootcamp con Cascade Condicional
# Verifica que capacidades y tecnologías se eliminen solo si no están referenciadas

Write-Host "`n===========================================" -ForegroundColor Cyan
Write-Host "PRUEBA HU6 - DELETE BOOTCAMP CON CASCADE" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8083/api/bootcamp"

# Función para hacer request con manejo de errores
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
        return $null
    }
}

Write-Host "`n=== PASO 1: Consultar estado inicial ===" -ForegroundColor Yellow
$bootcampsInicial = Invoke-SafeRequest -Uri $baseUrl
Write-Host "Bootcamps existentes: $($bootcampsInicial.Count)" -ForegroundColor White

# Mostrar bootcamps con sus capacidades
foreach ($b in $bootcampsInicial) {
    Write-Host "  - ID: $($b.id), Nombre: $($b.nombre), Capacidades: $($b.capacidades.Count)" -ForegroundColor Gray
    foreach ($c in $b.capacidades) {
        Write-Host "    * Capacidad ID: $($c.id), Nombre: $($c.nombre)" -ForegroundColor DarkGray
    }
}

Write-Host "`n=== PASO 2: Identificar bootcamp para eliminar ===" -ForegroundColor Yellow
# Buscar un bootcamp con pocas capacidades (menos referencias)
$bootcampAEliminar = $bootcampsInicial | Where-Object { $_.capacidades.Count -le 2 } | Select-Object -First 1

if ($null -eq $bootcampAEliminar) {
    Write-Host "ERROR: No se encontró un bootcamp adecuado para la prueba" -ForegroundColor Red
    exit
}

Write-Host "Bootcamp seleccionado para eliminar:" -ForegroundColor White
Write-Host "  ID: $($bootcampAEliminar.id)" -ForegroundColor White
Write-Host "  Nombre: $($bootcampAEliminar.nombre)" -ForegroundColor White
Write-Host "  Capacidades asociadas: $($bootcampAEliminar.capacidades.Count)" -ForegroundColor White

$capacidadesDelBootcamp = $bootcampAEliminar.capacidades | ForEach-Object { $_.id }
Write-Host "  IDs de capacidades: $($capacidadesDelBootcamp -join ', ')" -ForegroundColor White

Write-Host "`n=== PASO 3: Verificar referencias de capacidades ===" -ForegroundColor Yellow
# Verificar cuántos bootcamps usan cada capacidad
$otrosBootcamps = $bootcampsInicial | Where-Object { $_.id -ne $bootcampAEliminar.id }
$capacidadesCompartidas = @()
$capacidadesUnicas = @()

foreach ($capId in $capacidadesDelBootcamp) {
    $referencias = 0
    foreach ($otroB in $otrosBootcamps) {
        if ($otroB.capacidades.id -contains $capId) {
            $referencias++
        }
    }
    
    if ($referencias -gt 0) {
        $capacidadesCompartidas += $capId
        Write-Host "  - Capacidad $capId: COMPARTIDA (usada por $referencias bootcamp(s) más)" -ForegroundColor Yellow
    } else {
        $capacidadesUnicas += $capId
        Write-Host "  - Capacidad $capId: ÚNICA (solo usada por este bootcamp)" -ForegroundColor Green
    }
}

Write-Host "`nResumen:" -ForegroundColor White
Write-Host "  Capacidades compartidas (NO se deben eliminar): $($capacidadesCompartidas -join ', ')" -ForegroundColor Yellow
Write-Host "  Capacidades únicas (SÍ se deben eliminar): $($capacidadesUnicas -join ', ')" -ForegroundColor Green

Write-Host "`n=== PASO 4: Ejecutar DELETE bootcamp ===" -ForegroundColor Yellow
Write-Host "Eliminando bootcamp con ID: $($bootcampAEliminar.id)..." -ForegroundColor White

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/$($bootcampAEliminar.id)" -Method Delete -ErrorAction Stop
    Write-Host "✓ DELETE ejecutado exitosamente (204 No Content esperado)" -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 204) {
        Write-Host "✓ DELETE ejecutado exitosamente (204 No Content)" -ForegroundColor Green
    } else {
        Write-Host "ERROR: Falló la eliminación. Status: $statusCode" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        exit
    }
}

Start-Sleep -Seconds 2

Write-Host "`n=== PASO 5: Verificar que el bootcamp fue eliminado ===" -ForegroundColor Yellow
$bootcampEliminado = Invoke-SafeRequest -Uri "$baseUrl/$($bootcampAEliminar.id)"

if ($null -eq $bootcampEliminado) {
    Write-Host "✓ Bootcamp eliminado correctamente (404 Not Found)" -ForegroundColor Green
} else {
    Write-Host "✗ ERROR: El bootcamp aún existe" -ForegroundColor Red
}

Write-Host "`n=== PASO 6: Verificar estado de capacidades ===" -ForegroundColor Yellow

# Verificar capacidades compartidas (NO deben eliminarse)
if ($capacidadesCompartidas.Count -gt 0) {
    Write-Host "`nCapacidades COMPARTIDAS (deben seguir existiendo):" -ForegroundColor Yellow
    foreach ($capId in $capacidadesCompartidas) {
        $capacidad = Invoke-SafeRequest -Uri "http://localhost:8082/api/capacidad/$capId"
        if ($null -ne $capacidad) {
            Write-Host "  ✓ Capacidad $capId ($($capacidad.nombre)) - EXISTE (correcto)" -ForegroundColor Green
        } else {
            Write-Host "  ✗ Capacidad $capId - NO EXISTE (ERROR: no debió eliminarse)" -ForegroundColor Red
        }
    }
} else {
    Write-Host "No hay capacidades compartidas en este test" -ForegroundColor Gray
}

# Verificar capacidades únicas (DEBEN eliminarse)
if ($capacidadesUnicas.Count -gt 0) {
    Write-Host "`nCapacidades ÚNICAS (deben eliminarse):" -ForegroundColor Green
    foreach ($capId in $capacidadesUnicas) {
        $capacidad = Invoke-SafeRequest -Uri "http://localhost:8082/api/capacidad/$capId"
        if ($null -eq $capacidad) {
            Write-Host "  ✓ Capacidad $capId - NO EXISTE (correcto, fue eliminada)" -ForegroundColor Green
        } else {
            Write-Host "  ✗ Capacidad $capId ($($capacidad.nombre)) - EXISTE (ERROR: debió eliminarse)" -ForegroundColor Red
        }
    }
} else {
    Write-Host "No hay capacidades únicas en este test" -ForegroundColor Gray
}

Write-Host "`n=== PASO 7: Verificar bootcamps restantes ===" -ForegroundColor Yellow
$bootcampsFinal = Invoke-SafeRequest -Uri $baseUrl
Write-Host "Bootcamps después de eliminación: $($bootcampsFinal.Count)" -ForegroundColor White
Write-Host "Diferencia: $($bootcampsInicial.Count - $bootcampsFinal.Count) bootcamp(s) eliminado(s)" -ForegroundColor White

if (($bootcampsInicial.Count - $bootcampsFinal.Count) -eq 1) {
    Write-Host "✓ Cantidad correcta de bootcamps eliminados" -ForegroundColor Green
} else {
    Write-Host "✗ ERROR: Se esperaba eliminar 1 bootcamp" -ForegroundColor Red
}

Write-Host "`n===========================================" -ForegroundColor Cyan
Write-Host "RESUMEN DE PRUEBAS HU6" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan

$totalPruebas = 3
$pruebasExitosas = 0

# Prueba 1: Bootcamp eliminado
if ($null -eq $bootcampEliminado) {
    Write-Host "✓ Bootcamp eliminado correctamente" -ForegroundColor Green
    $pruebasExitosas++
} else {
    Write-Host "✗ Bootcamp NO eliminado" -ForegroundColor Red
}

# Prueba 2: Capacidades compartidas conservadas
$capacidadesCompartidasOK = $true
foreach ($capId in $capacidadesCompartidas) {
    $cap = Invoke-SafeRequest -Uri "http://localhost:8082/api/capacidad/$capId"
    if ($null -eq $cap) {
        $capacidadesCompartidasOK = $false
    }
}
if ($capacidadesCompartidas.Count -eq 0 -or $capacidadesCompartidasOK) {
    Write-Host "✓ Capacidades compartidas conservadas correctamente" -ForegroundColor Green
    $pruebasExitosas++
} else {
    Write-Host "✗ Capacidades compartidas eliminadas incorrectamente" -ForegroundColor Red
}

# Prueba 3: Capacidades únicas eliminadas
$capacidadesUnicasOK = $true
foreach ($capId in $capacidadesUnicas) {
    $cap = Invoke-SafeRequest -Uri "http://localhost:8082/api/capacidad/$capId"
    if ($null -ne $cap) {
        $capacidadesUnicasOK = $false
    }
}
if ($capacidadesUnicas.Count -eq 0 -or $capacidadesUnicasOK) {
    Write-Host "✓ Capacidades únicas eliminadas correctamente" -ForegroundColor Green
    $pruebasExitosas++
} else {
    Write-Host "✗ Capacidades únicas NO eliminadas" -ForegroundColor Red
}

Write-Host "`nResultado: $pruebasExitosas/$totalPruebas pruebas exitosas" -ForegroundColor $(if ($pruebasExitosas -eq $totalPruebas) { "Green" } else { "Yellow" })
Write-Host "===========================================" -ForegroundColor Cyan
