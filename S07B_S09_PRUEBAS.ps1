# S09 - Pruebas end-to-end, para PowerShell 7/Windows PowerShell 5.1
# PRECONDICION: Config, Eureka, Gateway, Keycloak, Cliente, Catalogo, Orden,
# Pago, Kafka y todas las bases en ejecución. Ejecutar desde la raiz del proyecto.
# No genera evidencias ficticias: cada assert consulta el sistema en ejecución.
param([int]$ProductoId = 1, [int]$Cantidad = 1)
$ErrorActionPreference = 'Stop'
$gateway = 'http://localhost:18080'

function Exigir($ok, $mensaje) {
    if (-not $ok) { throw "ERROR S09: $mensaje" }
    Write-Host "OK: $mensaje" -ForegroundColor Green
}
function Obtener-Orden([long]$id) {
    return Invoke-RestMethod -Method Get -Uri "$gateway/api/v1/ordenes/$id" -Headers $headers
}
function Esperar-Estado([long]$id, [string]$esperado) {
    for ($i = 0; $i -lt 24; $i++) {
        $actual = Obtener-Orden $id
        if ($actual.estado -eq $esperado) { return $actual }
        Start-Sleep -Seconds 1
    }
    throw "Orden $id no alcanzó estado $esperado. Estado actual: $($actual.estado)"
}
function Obtener-Pago([long]$idOrden) {
    $pagos = @(Invoke-RestMethod -Method Get -Uri 'http://localhost:8086/api/v1/pagos')
    return @($pagos | Where-Object { $_.ordenId -eq $idOrden })
}
function Nuevo-Pedido([string]$metodo) {
    $body = @{ metodoPago=$metodo; detalles=@(@{idProducto=$ProductoId; cantidad=$Cantidad}) } | ConvertTo-Json -Depth 5 -Compress
    return Invoke-RestMethod -Method Post -Uri "$gateway/api/v1/ordenes" -Headers $headers -ContentType 'application/json' -Body $body
}
function Repetir-Evento([long]$id, [string]$topic, [hashtable]$evento) {
    $payload = $evento | ConvertTo-Json -Depth 10 -Compress
    $linea = "${id}|${payload}"
    $linea | docker exec -i pagatu-kafka-dev /opt/kafka/bin/kafka-console-producer.sh `
        --bootstrap-server localhost:9092 --topic $topic --property parse.key=true --property 'key.separator=|'
    if ($LASTEXITCODE -ne 0) { throw "No se pudo reproducir evento en Kafka ($topic)" }
}

Write-Host '=== S09: Consistencia distribuida en Pagatu ===' -ForegroundColor Cyan
. "$PSScriptRoot/S07B_PKCE.ps1"
$tokenCliente = Get-PagatuKeycloakToken -Cuenta 'CLIENTE'
Exigir (-not [string]::IsNullOrEmpty($tokenCliente)) 'JWT obtenido de Keycloak con PKCE'
$headers = @{Authorization="Bearer $tokenCliente"}

$stockInicial = (Invoke-RestMethod "$gateway/api/v1/productos/$ProductoId").stock
Write-Host "Stock inicial producto $ProductoId : $stockInicial"

# Camino normal S08: el evento orden.pagada debe mantenerse.
$ordenOk = Nuevo-Pedido 'YAPE_PLIN'
$ordenOkFinal = Esperar-Estado $ordenOk.id 'PAGADA'
$pagosOk = @(Obtener-Pago $ordenOk.id)
Exigir ($pagosOk.Count -eq 1 -and $pagosOk[0].estado -eq 'VALIDADO') 'Orden PAGADA, un pago VALIDADO'
$stockPagado = (Invoke-RestMethod "$gateway/api/v1/productos/$ProductoId").stock
Exigir ($stockPagado -eq ($stockInicial - $Cantidad)) 'Stock reservado y retenido al pagar'

# Reproducir el mensaje original de la orden por Kafka y verificar idempotencia.
$ordenCreada = @{
    tipoEvento='orden.creada'; ordenId=[long]$ordenOk.id; idCliente=[string]$ordenOk.idCliente
    total=$ordenOk.total; metodoPago='YAPE_PLIN'; origen='pagatu-orden-ms'
    timestamp=[DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
}
Repetir-Evento $ordenOk.id 'orden-eventos' $ordenCreada
Start-Sleep -Seconds 3
Exigir (@(Obtener-Pago $ordenOk.id).Count -eq 1) 'Evento orden.creada duplicado: existe un solo pago'

# Camino de rechazo y compensación.
$stockAntesRechazo = (Invoke-RestMethod "$gateway/api/v1/productos/$ProductoId").stock
$ordenRechazada = Nuevo-Pedido 'TARJETA_RECHAZADA'
$ordenCancelada = Esperar-Estado $ordenRechazada.id 'CANCELADA'
$pagosFallidos = @(Obtener-Pago $ordenRechazada.id)
Exigir ($pagosFallidos.Count -eq 1 -and $pagosFallidos[0].estado -eq 'FALLIDO') 'Pago FALLIDO y orden CANCELADA'
$stockCompensado = (Invoke-RestMethod "$gateway/api/v1/productos/$ProductoId").stock
Exigir ($stockCompensado -eq $stockAntesRechazo) 'Stock restituido tras la compensación'

# Duplicación de compensación: la orden NO debe reponer dos veces el stock.
$pagoFallidoEvento = @{
    tipoEvento='pago.fallido'; ordenId=[long]$ordenRechazada.id; monto=$ordenRechazada.total
    estado='FALLIDO'; origen='pagatu-pago-ms'; timestamp=[DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
}
Repetir-Evento $ordenRechazada.id 'pago-eventos' $pagoFallidoEvento
Start-Sleep -Seconds 3
Exigir ((Obtener-Orden $ordenRechazada.id).estado -eq 'CANCELADA') 'Compensación duplicada: sigue CANCELADA'
Exigir (((Invoke-RestMethod "$gateway/api/v1/productos/$ProductoId").stock) -eq $stockAntesRechazo) `
    'Compensación duplicada: stock no se devuelve dos veces'

Write-Host '=== Pruebas S09 verificadas contra el entorno real ===' -ForegroundColor Green
Write-Host "Orden aprobada: $($ordenOk.id); Orden compensada: $($ordenRechazada.id)"
Write-Host 'Para evidencias de la guía: captura la consola, los logs de Pago/Orden, Kafka UI y SQL.'
