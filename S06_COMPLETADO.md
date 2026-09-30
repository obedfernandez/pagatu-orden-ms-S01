# S06 completado — Comunicación síncrona resiliente entre servicios

Este proyecto deja implementados los componentes de la sesión S06:

- `pagatu-orden-ms`: CRUD de órdenes, Config Client, Eureka Client, OpenFeign hacia `pagatu-catalogo-ms`, Circuit Breaker `catalogo`, fallback y trazabilidad con `X-Trace-ID`.
- `pagatu-cliente-ms`: actividad autónoma completa con PostgreSQL propio, Flyway, entidad/DTO/mapper/repositorio/servicio/controlador, Config Client, Eureka Client, Actuator/Prometheus y trazabilidad.
- `pagatu-gateway`: ruta `/api/v1/clientes/**` agregada además de catálogo y órdenes.
- `pagatu-config`: configuración externa DEV/PROD para `pagatu-cliente-ms`.

## 1. Levantar bases de datos

PowerShell, desde la raíz del proyecto:

```powershell
cd services/pagatu-catalogo-ms
docker compose -f compose-dev.yml up -d
cd ../pagatu-orden-ms
docker compose -f compose-dev.yml up -d
cd ../pagatu-cliente-ms
docker compose -f compose-dev.yml up -d
cd ../..
```

Puertos esperados:

- catálogo DB: `15432`
- cliente DB: `15433`
- orden DB: `15434`

## 2. Levantar infraestructura

Abre tres terminales:

```powershell
cd infra/pagatu-config
.\mvnw.cmd spring-boot:run
```

```powershell
cd infra/pagatu-eureka
.\mvnw.cmd spring-boot:run
```

```powershell
cd infra/pagatu-gateway
.\mvnw.cmd spring-boot:run
```

Comprueba:

- Config Server: `http://localhost:18888/pagatu-cliente-ms/dev`
- Eureka: `http://localhost:18761`
- Gateway: `http://localhost:18080`

## 3. Levantar microservicios

Abre tres terminales más:

```powershell
cd services/pagatu-catalogo-ms
.\mvnw.cmd spring-boot:run
```

```powershell
cd services/pagatu-orden-ms
.\mvnw.cmd spring-boot:run
```

```powershell
cd services/pagatu-cliente-ms
.\mvnw.cmd spring-boot:run
```

En Eureka deben aparecer:

- `PAGATU-CATALOGO-MS`
- `PAGATU-ORDEN-MS`
- `PAGATU-CLIENTE-MS`
- `PAGATU-GATEWAY`

## 4. Probar `pagatu-cliente-ms`

Listar seeds por Gateway:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:18080/api/v1/clientes"
```

Crear persona natural:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:18080/api/v1/clientes" `
  -ContentType "application/json" `
  -Body '{"tipoPersona":"NATURAL","dni":"12345678","nombreCompleto":"Cliente Prueba","direccion":"Puno","email":"cliente@example.com","whatsapp":"999111222"}'
```

Crear persona jurídica:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:18080/api/v1/clientes" `
  -ContentType "application/json" `
  -Body '{"tipoPersona":"JURIDICA","ruc":"20987654321","razonSocial":"Empresa Demo SAC","direccion":"Puno","email":"empresa@example.com","whatsapp":"988111222"}'
```

## 5. Evidencia Feign de S06

Con catálogo activo:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:18080/api/v1/ordenes" `
  -ContentType "application/json" `
  -Body '{"idCliente":1,"metodoPago":"YAPE_PLIN","detalles":[{"idProducto":1,"cantidad":2}]}'
```

Resultado esperado: `estado = PENDIENTE_PAGO`, con `nombreProducto`, `precioUnitario`, `subtotal` y `total` calculados.

## 6. Evidencia Circuit Breaker

Detén solo `pagatu-catalogo-ms` con `Ctrl+C` y repite la petición anterior al menos 5 veces. La configuración DEV deja `sliding-window-size: 5` y `minimum-number-of-calls: 5` para que la apertura sea observable durante la práctica.

Resultado esperado: HTTP `201`, `estado = CARRITO`, `total = null`, y la línea no validada con `nombreProducto = null` y `precioUnitario = null`. En `services/pagatu-orden-ms/logs/orden.log` debe verse `Fallback activado` junto al `traceId`.

Puedes consultar las métricas del Circuit Breaker con:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8082/actuator/metrics/resilience4j.circuitbreaker.state"
```

Tras unos 10 segundos, vuelve a iniciar catálogo y realiza nuevas llamadas para observar la recuperación del Circuit Breaker.

## 7. Capturas necesarias para el PDF de entrega

Las capturas deben hacerlas tú en tu PC porque la guía exige que se vea tu reloj del sistema y tu usuario/perfil. Conviene capturar:

1. Eureka con los servicios registrados.
2. `pagatu-cliente-ms` corriendo y una petición CRUD exitosa.
3. Orden exitosa con catálogo activo (`PENDIENTE_PAGO`).
4. Orden con catálogo detenido (`CARRITO`, `total: null`).
5. Log de `pagatu-orden-ms` con `traceId` y `Fallback activado`.
6. Estado del Circuit Breaker durante la prueba y recuperación.
