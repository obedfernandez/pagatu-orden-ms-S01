# S08 - Pasos de ejecución y evidencia

Esta guía está preparada para Windows PowerShell y sigue el flujo de la sesión S08.

## 1. Levantar Kafka y Kafka UI

Desde la raíz del proyecto:

```powershell
cd kafka
docker compose -f compose-dev.yml up -d
docker compose -f compose-dev.yml ps
cd ..
```

Kafka UI: `http://localhost:18085`.

## 2. Crear y probar `orden-eventos` manualmente

```powershell
docker compose -f kafka/compose-dev.yml exec kafka /opt/kafka/bin/kafka-topics.sh --create --topic orden-eventos --bootstrap-server kafka:9092 --partitions 3 --replication-factor 1
docker compose -f kafka/compose-dev.yml exec kafka /opt/kafka/bin/kafka-topics.sh --list --bootstrap-server kafka:9092
```

Consumer de consola:

```powershell
docker compose -f kafka/compose-dev.yml exec kafka /opt/kafka/bin/kafka-console-consumer.sh --topic orden-eventos --bootstrap-server kafka:9092 --from-beginning
```

En otra terminal, producer de consola:

```powershell
docker compose -f kafka/compose-dev.yml exec kafka /opt/kafka/bin/kafka-console-producer.sh --topic orden-eventos --bootstrap-server kafka:9092
```

Puedes publicar `hola kafka` y luego un JSON con el contrato de `orden.creada`.

## 3. Probar el cliente Python de uso rápido

```powershell
cd uso-rapido/pagatu-eventos-py
docker compose up -d --build
docker compose exec pagatu-eventos-py python /app/consumer_ordenes.py
```

En otra terminal:

```powershell
cd uso-rapido/pagatu-eventos-py
docker compose exec pagatu-eventos-py python /app/producer_ordenes.py
```

El producer publica cada 2 segundos. El consumer imprime una línea JSON por mensaje con `topic`, `partition`, `offset`, `eventType`, `ordenId`, `idCliente`, `metodoPago` y `status`.

## 4. Levantar PostgreSQL de pagos

```powershell
cd services/pagatu-pago-ms
docker compose -f compose-dev.yml up -d
docker exec -it pagatu-postgres-pago-dev psql -U pagatu -d pagatu_pago_db -c "SELECT current_database();"
cd ../..
```

## 5. Levantar el resto del entorno DEV

Mantén las bases de datos existentes de S07 activas. Luego inicia Config Server, Eureka y los servicios en terminales separadas. En cada proyecto puedes usar:

```powershell
.\mvnw.cmd spring-boot:run
```

Orden recomendado:

1. `infra/pagatu-config`
2. `infra/pagatu-eureka`
3. `services/pagatu-auth-ms`
4. `services/pagatu-catalogo-ms`
5. `services/pagatu-cliente-ms`
6. `services/pagatu-orden-ms`
7. `services/pagatu-pago-ms`
8. `infra/pagatu-gateway`

Comprueba en Eureka que aparezca `PAGATU-PAGO-MS`.

## 6. Prueba punta a punta

Con todo levantado:

```powershell
.\S08_PRUEBAS.ps1
```

Debes observar el flujo:

`orden.creada` -> pago `VALIDADO` -> `pago.validado` -> orden `PAGADA` -> `orden.pagada` -> registro de notificación/comprobante en `pagatu-cliente-ms`.

## 7. Comprobar pagos

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:18080/api/v1/pagos"
```

Para un pago concreto:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:18080/api/v1/pagos/1"
```

## 8. Prueba de desacople principal

1. Detén `pagatu-pago-ms`.
2. Crea una orden; debe responder `201` y quedar `PENDIENTE_PAGO`.
3. En Kafka UI revisa el consumer group `pagatu-pago-ms`: debe haber lag pendiente.
4. Vuelve a iniciar `pagatu-pago-ms`.
5. Comprueba `status=consumed` y después que la orden queda `PAGADA`.

## 9. Prueba del evento autónomo

Evento adicional implementado: `orden.pagada`.

1. Detén `pagatu-cliente-ms`.
2. Crea una orden y deja que pagos la procese.
3. En Kafka UI abre `orden-pagada-eventos` y revisa el grupo `pagatu-cliente-ms-notificaciones`.
4. Vuelve a levantar `pagatu-cliente-ms`.
5. Debe consumir el evento y registrar la fila pendiente.

Consulta:

```powershell
docker exec -it pagatu-postgres-cliente-dev psql -U pagatu -d pagatu_cliente_db -c "SELECT id, orden_id, id_cliente, monto, canal, estado, fecha_registro FROM notificaciones_pago ORDER BY id DESC;"
```

## 10. Archivos de apoyo para la entrega

- `docs/S08_CONTRATOS_EVENTOS.md`: contratos de los tres eventos.
- `S08_EVIDENCIA_GUIA.md`: capturas que debes tomar para la evidencia.
- `S08_PRUEBAS.ps1`: prueba de punta a punta.
- `S08_COMPLETADO.md`: resumen de lo implementado.

Las capturas del informe deben hacerse en tu propio equipo mientras ejecutas el proyecto; no se incluyen capturas simuladas.
