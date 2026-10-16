# Ejecutar ANTES de iniciar Orden y Cliente con las migraciones Flyway de S07b.
# Copia backups sin borrar ni restaurar nada.
$ErrorActionPreference='Stop'
$salida=Join-Path $PSScriptRoot 'respaldos-s07b'
New-Item -ItemType Directory -Force -Path $salida | Out-Null
$fecha=Get-Date -Format 'yyyyMMdd_HHmmss'
@(
    @{Contenedor='pagatu-postgres-orden-dev';Base='pagatu_orden_db'},
    @{Contenedor='pagatu-postgres-cliente-dev';Base='pagatu_cliente_db'}
) | ForEach-Object {
    $ct=$_.Contenedor; $db=$_.Base
    $enContenedor="/tmp/${db}_${fecha}.dump"
    docker exec $ct pg_dump -U pagatu -d $db -Fc -f $enContenedor
    if ($LASTEXITCODE -ne 0) { throw "No se pudo respaldar $db" }
    docker cp "${ct}:${enContenedor}" (Join-Path $salida "${db}_${fecha}.dump")
    if ($LASTEXITCODE -ne 0) { throw "No se pudo copiar el respaldo de $db" }
    docker exec $ct rm -f $enContenedor | Out-Null
    Write-Host "OK: respaldo $db en $salida" -ForegroundColor Green
}
