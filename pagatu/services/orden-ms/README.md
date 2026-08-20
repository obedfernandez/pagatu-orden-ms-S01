# orden-ms — Pagatu

Microservicio autónomo para gestionar órdenes. Sigue el patrón de S01: REST + capas + PostgreSQL + Flyway + validación + Swagger + Actuator + trazabilidad.

## Dominio
`orden-ms` administra órdenes. Conserva `clienteId` y `conceptoId` solo como referencias externas; no administra clientes ni el catálogo.

## Requisitos
- Java 21
- Docker Desktop
- Internet la primera vez que Maven Wrapper descargue Maven/dependencias

## Ejecutar en DEV (Windows PowerShell)
```powershell
cd services/orden-ms
docker compose -f compose-dev.yml up -d
.\mvnw.cmd spring-boot:run
```

Swagger: http://localhost:8080/swagger-ui.html  
Health: http://localhost:8080/actuator/health  
Metrics: http://localhost:8080/actuator/metrics

## CRUD PowerShell
Crear:
```powershell
$body = @{ clienteId=1; conceptoId=10; cantidad=2; precioUnitario=15.50; estado="CREADA" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/ordenes" -ContentType "application/json" -Body $body
```
Listar:
```powershell
Invoke-RestMethod http://localhost:8080/api/v1/ordenes
```
Actualizar:
```powershell
$body = @{ clienteId=1; conceptoId=10; cantidad=3; precioUnitario=15.50; estado="CONFIRMADA" } | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri "http://localhost:8080/api/v1/ordenes/1" -ContentType "application/json" -Body $body
```
Eliminar:
```powershell
Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/v1/ordenes/1"
```

## PostgreSQL / Flyway
```powershell
docker exec -it pagatu-postgres-orden-dev psql -U pagatu -d pagatu_orden_db -c "\dt"
docker exec -it pagatu-postgres-orden-dev psql -U pagatu -d pagatu_orden_db -c "SELECT * FROM flyway_schema_history;"
docker exec -it pagatu-postgres-orden-dev psql -U pagatu -d pagatu_orden_db -c "SELECT * FROM ordenes;"
```

## Dos instancias (escalamiento horizontal)
Terminal 1:
```powershell
.\mvnw.cmd spring-boot:run
```
Terminal 2:
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```
Verificación:
```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8081/actuator/health
```
Ambas instancias son copias del mismo servicio y usan la misma base DEV; el puerto se sobreescribe al arrancar, sin cambiar el código.
