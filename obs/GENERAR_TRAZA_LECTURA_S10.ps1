# Demostracion segura: misma cabecera X-Trace-ID en consultas de lectura.
# Dos GET independientes NO prueban por si solos propagacion end-to-end entre servicios.
[CmdletBinding()]
param([string]$Catalogo='http://localhost:8080',[string]$Orden='http://localhost:8082',[string]$TokenOrden='')
$ErrorActionPreference='Continue'
$trace=[guid]::NewGuid().ToString()
$headers=@{'X-Trace-ID'=$trace}
Write-Host "TraceId de lectura: $trace" -ForegroundColor Cyan
try {
 $r=Invoke-WebRequest -Uri "$Catalogo/api/v1/productos" -Headers $headers -UseBasicParsing -TimeoutSec 10
 Write-Host "Catalogo GET: HTTP $($r.StatusCode)" -ForegroundColor Green
} catch { Write-Warning "Catalogo GET: $($_.Exception.Message)" }
if ($TokenOrden) {
 try {
  $h=@{'X-Trace-ID'=$trace;Authorization="Bearer $TokenOrden"}
  $r=Invoke-WebRequest -Uri "$Orden/api/v1/ordenes" -Headers $h -UseBasicParsing -TimeoutSec 10
  Write-Host "Orden GET: HTTP $($r.StatusCode)" -ForegroundColor Green
 } catch { Write-Warning "Orden GET: $($_.Exception.Message)" }
} else { Write-Host 'Orden GET no ejecutado: requiere JWT autorizado; no introducirlo en capturas ni informes.' }
Write-Host 'Loki Explore: {application=~"pagatu-(catalogo|orden)-ms"} |~ "' -NoNewline
Write-Host $trace -NoNewline
Write-Host '"'
Write-Host 'Los logs de API usan obs_http y traceId; esperar la ingestion de Promtail antes de buscar.'
Write-Host 'Para demostrar una llamada distribuida real se necesita una transaccion de prueba aislada Orden->Feign->Catalogo.'
