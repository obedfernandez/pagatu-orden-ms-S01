# S08 completada - Mensajería asíncrona entre servicios

Este proyecto parte de S07 y agrega la implementación de S08:

- Kafka 4.3.1 en KRaft y Kafka UI.
- DEV: `19092` / `18085`.
- PROD local: `29092` / `28085`.
- Topics de 3 particiones: `orden-eventos`, `pago-eventos` y el evento autónomo `orden-pagada-eventos`.
- `pagatu-pago-ms` con PostgreSQL `pagatu_pago_db`, Flyway, Config Server, Eureka, Actuator, JWT Resource Server y endpoints de consulta.
- Flujo `orden.creada` -> pago `VALIDADO` -> `pago.validado` -> orden `PAGADA`.
- Publicación de eventos después del commit.
- Logs de publicación/consumo compatibles con la evidencia de S08.
- Uso rápido con productor/consumidor Python.
- Evento autónomo `orden.pagada`, consumido por `pagatu-cliente-ms`, que registra `notificaciones_pago`.
- Contratos en `docs/S08_CONTRATOS_EVENTOS.md`.
- Pruebas PowerShell en `S08_PRUEBAS.ps1`.

## Orden sugerido para levantar DEV

1. Bases de datos existentes y nueva BD de pagos.
2. `infra/pagatu-config`.
3. `infra/pagatu-eureka`.
4. `kafka/compose-dev.yml`.
5. `pagatu-auth-ms`, `pagatu-catalogo-ms`, `pagatu-cliente-ms`, `pagatu-orden-ms` y `pagatu-pago-ms`.
6. `infra/pagatu-gateway`.

Kafka UI: `http://localhost:18085`.
