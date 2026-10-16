# Pruebas de seguridad Keycloak (PowerShell). Pide login en navegador mediante PKCE.
param([switch]$CrearOrden, [switch]$ProbarAdmin, [switch]$CrearProductoAdmin, [int]$ProductoId=1)
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/S07B_PKCE.ps1"
$gateway='http://localhost:18080'
function StatusHttp([string]$Metodo, [string]$Url, $Token, $Cuerpo=$null) {
    $headers=@{}
    if ($Token) { $headers.Authorization="Bearer $Token" }
    try {
        $args=@{Method=$Metodo;Uri=$Url;Headers=$headers;ErrorAction='Stop'}
        if ($null -ne $Cuerpo) { $args.ContentType='application/json'; $args.Body=$Cuerpo }
        $response=Invoke-WebRequest @args
        return [int]$response.StatusCode
    } catch {
        if ($_.Exception.Response) { return [int]$_.Exception.Response.StatusCode }
        throw
    }
}
function AssertHttp([string]$Nombre,[int]$Real,[int]$Esperado) {
    if ($Real -ne $Esperado) { throw "${Nombre}: esperaba HTTP $Esperado ..." }
    Write-Host "OK $Nombre : HTTP $Real" -ForegroundColor Green
}
Write-Host '=== S07b Keycloak ===' -ForegroundColor Cyan
$discovery=Invoke-RestMethod 'http://localhost:18180/realms/pagatu/.well-known/openid-configuration'
if ($discovery.issuer -ne 'http://localhost:18180/realms/pagatu') {throw 'Issuer distinto: revisar KC_HOSTNAME y URLs.'}
Write-Host 'OK: OIDC discovery y issuer' -ForegroundColor Green
$tokenCliente=Get-PagatuKeycloakToken -Cuenta 'CLIENTE'
function RevisarClaims([string]$Token,[string]$Rol) {
    $fragmento=$Token.Split('.')[1].Replace('-','+').Replace('_','/')
    $fragmento=$fragmento.PadRight($fragmento.Length + (4 - $fragmento.Length % 4) % 4,'=')
    $claims=[Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($fragmento)) | ConvertFrom-Json
    if (-not ($claims.realm_access.roles -contains $Rol)) { throw "Token sin rol $Rol. Revisar el client scope roles de Keycloak." }
    $uuid=[guid]::Empty
    if (-not [guid]::TryParse([string]$claims.sub,[ref]$uuid)) {throw 'El sub del token no parece un UUID.'}
    Write-Host "OK: token $Rol, sub=$($claims.sub)" -ForegroundColor Green
}
RevisarClaims $tokenCliente 'CLIENTE'
if ($CrearProductoAdmin -and -not $ProbarAdmin) {throw 'Para -CrearProductoAdmin especifica tambien -ProbarAdmin.'}
AssertHttp 'Orden sin token' (StatusHttp GET "$gateway/api/v1/ordenes" $null) 401
AssertHttp 'Producto creado por CLIENTE (prohibido)' (StatusHttp POST "$gateway/api/v1/productos" $tokenCliente '{"nombre":"SoloVerificarPermisos"}') 403
AssertHttp 'Ordenes con CLIENTE' (StatusHttp GET "$gateway/api/v1/ordenes" $tokenCliente) 200
if ($CrearOrden) {
    $cuerpo=@{metodoPago='YAPE_PLIN';detalles=@(@{idProducto=$ProductoId;cantidad=1})}|ConvertTo-Json -Depth 6
    $orden=Invoke-RestMethod -Method Post -Uri "$gateway/api/v1/ordenes" -Headers @{Authorization="Bearer $tokenCliente"} -ContentType 'application/json' -Body $cuerpo
    Write-Host "Orden creada: $($orden.id), idCliente (sub): $($orden.idCliente)" -ForegroundColor Green
}
if ($ProbarAdmin) {
    $tokenAdmin=Get-PagatuKeycloakToken -Cuenta 'ADMIN'
    RevisarClaims $tokenAdmin 'ADMIN'
    AssertHttp 'Ordenes con ADMIN' (StatusHttp GET "$gateway/api/v1/ordenes" $tokenAdmin) 200
    if ($CrearProductoAdmin) {
        $bodyProducto=@{nombre='Prueba S07b';descripcion='Creado como ADMIN';precio=10;activo=$true;stock=1;categoriaId=1}|ConvertTo-Json
        AssertHttp 'Crear producto con ADMIN' (StatusHttp POST "$gateway/api/v1/productos" $tokenAdmin $bodyProducto) 201
    }
}
Write-Host '=== Pruebas S07b completadas contra los servicios actuales ===' -ForegroundColor Green
