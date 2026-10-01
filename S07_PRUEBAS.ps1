$ErrorActionPreference = "Stop"
$gw = "http://localhost:18080"
$auth = "http://localhost:8085"
$ordenDirecta = "http://localhost:8082"
$clienteDirecto = "http://localhost:8084"

Write-Host "=== S07 Pagatu - pruebas principales ==="

# 3.14 - Login correcto de ADMIN y CLIENTE
$admin = Invoke-RestMethod -Method Post -Uri "$gw/api/v1/auth/login" -ContentType "application/json" -Body '{"email":"admin@pagatu.com","password":"admin123"}'
$cliente = Invoke-RestMethod -Method Post -Uri "$gw/api/v1/auth/login" -ContentType "application/json" -Body '{"email":"cliente@pagatu.com","password":"cliente123"}'
$tokenAdmin = $admin.access_token
$tokenCliente = $cliente.access_token
Write-Host "Login ADMIN: OK; token_type=$($admin.token_type); expires_in=$($admin.expires_in)"
Write-Host "Login CLIENTE: OK; token_type=$($cliente.token_type); expires_in=$($cliente.expires_in)"

# 3.14 - Login inválido -> 401
try {
  Invoke-RestMethod -Method Post -Uri "$gw/api/v1/auth/login" -ContentType "application/json" -Body '{"email":"admin@pagatu.com","password":"incorrecta"}' | Out-Null
  Write-Host "Login incorrecto: ERROR (se esperaba 401)"
} catch { Write-Host "Login incorrecto: $($_.Exception.Response.StatusCode.value__) (esperado 401)" }

# 3.15 - Registro 201, repetido 409 y validación 400
$emailNuevo = "s07.$([DateTimeOffset]::UtcNow.ToUnixTimeSeconds())@pagatu.com"
$registroBody = @{ email = $emailNuevo; password = "nuevo12345" } | ConvertTo-Json -Compress
try {
  $nuevo = Invoke-RestMethod -Method Post -Uri "$gw/api/v1/auth/registro" -ContentType "application/json" -Body $registroBody
  Write-Host "Registro nuevo: OK, id=$($nuevo.id), email=$($nuevo.email) (esperado 201)"
} catch { Write-Host "Registro nuevo: fallo inesperado $($_.Exception.Response.StatusCode.value__)" }
try {
  Invoke-RestMethod -Method Post -Uri "$gw/api/v1/auth/registro" -ContentType "application/json" -Body $registroBody | Out-Null
  Write-Host "Registro repetido: ERROR (se esperaba 409)"
} catch { Write-Host "Registro repetido: $($_.Exception.Response.StatusCode.value__) (esperado 409)" }
try {
  Invoke-RestMethod -Method Post -Uri "$gw/api/v1/auth/registro" -ContentType "application/json" -Body '{"email":"corto@pagatu.com","password":"abc"}' | Out-Null
  Write-Host "Registro inválido: ERROR (se esperaba 400)"
} catch { Write-Host "Registro inválido: $($_.Exception.Response.StatusCode.value__) (esperado 400)" }

# 3.11/3.12 - JWKS público
$jwks = Invoke-RestMethod -Method Get -Uri "$auth/.well-known/jwks.json"
Write-Host "JWKS publicado: $($jwks.keys.Count) clave(s); kty=$($jwks.keys[0].kty); alg=$($jwks.keys[0].alg)"

# 3.19 - Caso 1: sin token -> 401
try {
  Invoke-RestMethod -Method Get -Uri "$gw/api/v1/ordenes" | Out-Null
  Write-Host "Ordenes sin token: ERROR (se esperaba 401)"
} catch { Write-Host "Ordenes sin token: $($_.Exception.Response.StatusCode.value__) (esperado 401)" }

# 3.19 - Caso 2: token con firma alterada -> 401
$partes = $tokenAdmin.Split('.')
$primera = if ($partes[2][0] -eq 'X') { 'Y' } else { 'X' }
$tokenAlterado = "$($partes[0]).$($partes[1]).$primera$($partes[2].Substring(1))"
try {
  Invoke-RestMethod -Method Get -Uri "$gw/api/v1/ordenes" -Headers @{Authorization="Bearer $tokenAlterado"} | Out-Null
  Write-Host "Token alterado en Gateway: ERROR (se esperaba 401)"
} catch { Write-Host "Token alterado en Gateway: $($_.Exception.Response.StatusCode.value__) (esperado 401)" }

# 3.19 - Casos 3 y 4: CLIENTE 403 / ADMIN 201 en catálogo
$productoBody = '{"nombre":"Producto S07","descripcion":"Prueba seguridad","precio":10.0,"activo":true,"stock":5,"categoriaId":1}'
try {
  Invoke-RestMethod -Method Post -Uri "$gw/api/v1/productos" -Headers @{Authorization="Bearer $tokenCliente"} -ContentType "application/json" -Body $productoBody | Out-Null
  Write-Host "CLIENTE creando producto: ERROR (se esperaba 403)"
} catch { Write-Host "CLIENTE creando producto: $($_.Exception.Response.StatusCode.value__) (esperado 403)" }

try {
  $prod = Invoke-RestMethod -Method Post -Uri "$gw/api/v1/productos" -Headers @{Authorization="Bearer $tokenAdmin"} -ContentType "application/json" -Body $productoBody
  Write-Host "ADMIN creando producto: OK, id=$($prod.id) (esperado 201)"
} catch { Write-Host "ADMIN creando producto: fallo. Verifica categoriaId=1 y catalogo-ms." }

# 3.23 - El CLIENTE crea una orden sin idCliente en el body; la respuesta debe contener idCliente=1
$ordenBody = '{"metodoPago":"YAPE_PLIN","detalles":[{"idProducto":1,"cantidad":2}]}'
try {
  $ord = Invoke-RestMethod -Method Post -Uri "$gw/api/v1/ordenes" -Headers @{Authorization="Bearer $tokenCliente"} -ContentType "application/json" -Body $ordenBody
  Write-Host "Orden CLIENTE por Gateway: OK, idCliente=$($ord.idCliente) (debe ser 1 y no vino en el body)"
} catch { Write-Host "Orden CLIENTE: fallo. Verifica producto id=1 y servicios." }

# 3.23 - ADMIN tiene token válido pero no idCliente -> 400
try {
  Invoke-RestMethod -Method Post -Uri "$gw/api/v1/ordenes" -Headers @{Authorization="Bearer $tokenAdmin"} -ContentType "application/json" -Body $ordenBody | Out-Null
  Write-Host "Orden ADMIN sin idCliente: ERROR (se esperaba 400)"
} catch { Write-Host "Orden ADMIN sin idCliente: $($_.Exception.Response.StatusCode.value__) (esperado 400)" }

# 3.24 - Llamado directo al Resource Server
try {
  $ordDirecta = Invoke-RestMethod -Method Post -Uri "$ordenDirecta/api/v1/ordenes" -Headers @{Authorization="Bearer $tokenCliente"} -ContentType "application/json" -Body $ordenBody
  Write-Host "Orden directa con token CLIENTE válido: OK, idCliente=$($ordDirecta.idCliente) (esperado 201)"
} catch { Write-Host "Orden directa con token CLIENTE válido: fallo inesperado" }
try {
  Invoke-RestMethod -Method Post -Uri "$ordenDirecta/api/v1/ordenes" -Headers @{Authorization="Bearer token.invalido.manipulado"} -ContentType "application/json" -Body $ordenBody | Out-Null
  Write-Host "Orden directa token inválido: ERROR (se esperaba 401)"
} catch { Write-Host "Orden directa token inválido: $($_.Exception.Response.StatusCode.value__) (esperado 401)" }

# Sección 4 - pagatu-cliente-ms protegido
try {
  $me = Invoke-RestMethod -Method Get -Uri "$gw/api/v1/clientes/me" -Headers @{Authorization="Bearer $tokenCliente"}
  Write-Host "CLIENTE /clientes/me: OK, id=$($me.id)"
} catch { Write-Host "CLIENTE /clientes/me: fallo" }
try {
  Invoke-RestMethod -Method Get -Uri "$gw/api/v1/clientes" -Headers @{Authorization="Bearer $tokenCliente"} | Out-Null
  Write-Host "CLIENTE listando clientes: ERROR (se esperaba 403)"
} catch { Write-Host "CLIENTE listando clientes: $($_.Exception.Response.StatusCode.value__) (esperado 403)" }
try {
  $lista = Invoke-RestMethod -Method Get -Uri "$gw/api/v1/clientes" -Headers @{Authorization="Bearer $tokenAdmin"}
  Write-Host "ADMIN listando clientes: OK, cantidad=$($lista.Count)"
} catch { Write-Host "ADMIN listando clientes: fallo" }
try {
  Invoke-RestMethod -Method Get -Uri "$clienteDirecto/api/v1/clientes/me" -Headers @{Authorization="Bearer $tokenAlterado"} | Out-Null
  Write-Host "Cliente-ms directo token alterado: ERROR (se esperaba 401)"
} catch { Write-Host "Cliente-ms directo token alterado: $($_.Exception.Response.StatusCode.value__) (esperado 401)" }

Write-Host "=== Fin de pruebas ==="
Write-Host "Para 3.26 ejecuta .\\S07_REVOCACION_ROL.ps1 y para 3.27 .\\S07_PKCE.ps1"
