# Comprobaciones de lectura de Pagatu OBS, sin escrituras de negocio.
[CmdletBinding()]
param([string]$Evidencias = '')
$ErrorActionPreference='Continue'
$checks = @(
  @{Nombre='Config Server';Url='http://localhost:18888/actuator/health';Tipo='health'},
  @{Nombre='Eureka';Url='http://localhost:18761/actuator/health';Tipo='health'},
  @{Nombre='Catalogo health';Url='http://localhost:8080/actuator/health';Tipo='health'},
  @{Nombre='Catalogo readiness';Url='http://localhost:8080/actuator/health/readiness';Tipo='health'},
  @{Nombre='Orden readiness (si arrancado)';Url='http://localhost:8082/actuator/health/readiness';Tipo='health'},
  @{Nombre='Prometheus';Url='http://localhost:19090/-/ready';Tipo='http'},
  @{Nombre='Loki';Url='http://localhost:13100/ready';Tipo='http'},
  @{Nombre='Grafana';Url='http://localhost:13000/api/health';Tipo='http'}
)
$results=foreach($c in $checks){
  try {
    $r=Invoke-WebRequest -Uri $c.Url -TimeoutSec 7 -UseBasicParsing
    $obj=$null;try{$obj=$r.Content|ConvertFrom-Json}catch{}
    $state=if ($c.Tipo -eq 'health' -and $obj) {$obj.status} else {"HTTP $($r.StatusCode)"}
    [pscustomobject]@{Componente=$c.Nombre;Estado=$state;Http=$r.StatusCode;Url=$c.Url;Fecha=(Get-Date).ToString('o')}
  } catch { [pscustomobject]@{Componente=$c.Nombre;Estado='NO VERIFICADO';Http='';Url=$c.Url;Fecha=(Get-Date).ToString('o');Detalle=$_.Exception.Message} }
}
$results|Format-Table -AutoSize
try {
 $q=[uri]::EscapeDataString('up{job=~"pagatu-.*"}')
 $r=Invoke-RestMethod "http://localhost:19090/api/v1/query?query=$q" -TimeoutSec 10
 Write-Host "`nPrometheus targets actuales: " -ForegroundColor Cyan
 $r.data.result | ForEach-Object { "{0,-28} {1,-30} {2}" -f $_.metric.job,$_.metric.application,$_.value[1] }
} catch { Write-Warning "No se leyo Prometheus: $($_.Exception.Message)" }
if ($Evidencias) {
 New-Item -ItemType Directory -Path $Evidencias -Force | Out-Null
 $file=Join-Path $Evidencias ("health-s10-"+(Get-Date -Format 'yyyyMMdd-HHmmss')+'.json')
 $results | ConvertTo-Json -Depth 8 | Set-Content -Encoding utf8 $file
 Write-Host "Guardado: $file"
}
