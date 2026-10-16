$ErrorActionPreference = "Stop"

Write-Host "=== S08 Pagatu: prueba punta a punta ===" -ForegroundColor Cyan
Write-Host "Requisito: servicios, bases, Kafka y Kafka UI ya levantados." -ForegroundColor Yellow

$login = Invoke-RestMethod -Method Post -Uri "http://localhost:18080/api/v1/auth/login" `
  -ContentType "application/json" `
  -Body '{"email":"cliente@pagatu.com","password":"cliente123"}'
$tokenCliente = $login.access_token

$orden = Invoke-RestMethod -Method Post -Uri "http://localhost:18080/api/v1/ordenes" `
  -Headers @{ Authorization = "Bearer $tokenCliente" } `
  -ContentType "application/json" `
  -Body '{"metodoPago":"YAPE_PLIN","detalles":[{"idProducto":1,"cantidad":2}]}'

Write-Host "Orden creada: id=$($orden.id), estado inicial=$($orden.estado)"
Start-Sleep -Seconds 4

$ordenFinal = Invoke-RestMethod -Method Get -Uri "http://localhost:18080/api/v1/ordenes/$($orden.id)" `
  -Headers @{ Authorization = "Bearer $tokenCliente" }
Write-Host "Estado final de la orden: $($ordenFinal.estado)" -ForegroundColor Green

$pagos = Invoke-RestMethod -Method Get -Uri "http://localhost:18080/api/v1/pagos"
Write-Host "Pagos registrados: $($pagos.Count)"

Write-Host "`nVerifica en Kafka UI:" -ForegroundColor Cyan
Write-Host "- orden-eventos: orden.creada"
Write-Host "- pago-eventos: pago.validado"
Write-Host "- orden-pagada-eventos: orden.pagada"
Write-Host "- consumer groups con lag 0"
Write-Host "`nPara la prueba de desacople: apaga pagatu-pago-ms, crea otra orden, observa lag=1 y vuelve a levantarlo."
Write-Host "Para el evento autónomo: apaga pagatu-cliente-ms, genera una orden pagada, observa lag en pagatu-cliente-ms-notificaciones y vuelve a levantarlo."
