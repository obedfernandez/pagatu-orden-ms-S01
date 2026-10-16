# Vincula voluntariamente un cliente existente con el sub verificado de Keycloak.
# Ejemplo: .\S07B_VINCULAR_CLIENTE.ps1 -ClienteId 1 -Sub 'UUID-REAL-DEL-TOKEN'
param(
    [Parameter(Mandatory=$true)][long]$ClienteId,
    [Parameter(Mandatory=$true)][string]$Sub
)
$ErrorActionPreference='Stop'
$uuid=[Guid]::Empty
if (-not [Guid]::TryParse($Sub,[ref]$uuid)) {throw 'Sub debe ser un UUID valido.'}
$subCanonico=$uuid.ToString()
Write-Host "Se vinculara el cliente de negocio $ClienteId con Keycloak sub=$subCanonico."
Write-Host 'Verifica que el usuario de Keycloak es efectivamente el dueno de ese perfil.'
$confirmar=Read-Host 'Escribe VINCULAR para continuar'
if ($confirmar -cne 'VINCULAR') {Write-Host 'Sin cambios.';exit 0}
$sql="UPDATE clientes SET keycloak_sub = '$subCanonico' WHERE id = $ClienteId AND (keycloak_sub IS NULL OR keycloak_sub = '$subCanonico') RETURNING id, keycloak_sub;"
docker exec pagatu-postgres-cliente-dev psql -v ON_ERROR_STOP=1 -U pagatu -d pagatu_cliente_db -c $sql
if ($LASTEXITCODE -ne 0) {throw 'No se realizo la vinculacion.'}
Write-Host 'Comprueba que se devolvio 1 fila; si no, el cliente no existe o tiene otro vinculo.'
