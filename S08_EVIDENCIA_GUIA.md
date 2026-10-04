# Guía de evidencias S08

Captura siempre la pantalla completa con reloj y usuario/perfil visibles, tal como exige la sesión.

## 1. Kafka y pagatu-pago-ms

- Kafka UI en `http://localhost:18085` mostrando `orden-eventos` y `pago-eventos`, 3 particiones cada uno.
- Eureka mostrando `PAGATU-PAGO-MS`.
- PostgreSQL: tabla `pagos` con datos.

## 2. Flujo de eventos

Captura logs en este orden:

1. `pagatu-orden-ms`: `eventType=orden.creada ... status=published`.
2. `pagatu-pago-ms`: `eventType=orden.creada ... status=consumed`.
3. `pagatu-pago-ms`: `eventType=pago.validado ... status=published`.
4. `pagatu-orden-ms`: `eventType=pago.validado ... status=consumed`.
5. Consulta de la orden en estado `PAGADA`.

## 3. Desacople obligatorio

1. Apaga `pagatu-pago-ms`.
2. Crea una orden: debe responder `201` y quedar `PENDIENTE_PAGO`.
3. En Kafka UI observa lag del grupo `pagatu-pago-ms`.
4. Levanta pagos otra vez.
5. Captura consumo pendiente y orden en `PAGADA`.

## 4. Evento autónomo

Evento implementado: `orden.pagada` en `orden-pagada-eventos`.

- Productor: `pagatu-orden-ms`.
- Consumidor: `pagatu-cliente-ms`.
- Consumer group: `pagatu-cliente-ms-notificaciones`.
- Efecto: fila en `notificaciones_pago`.

Prueba de desacople:

1. Apaga `pagatu-cliente-ms`.
2. Crea una orden y deja que llegue a `PAGADA`.
3. Observa lag en `orden-pagada-eventos`.
4. Levanta `pagatu-cliente-ms`.
5. Captura el log de consumo y la fila en `notificaciones_pago`.

Consulta útil:

```sql
SELECT id, orden_id, id_cliente, monto, canal, estado, fecha_registro
FROM notificaciones_pago
ORDER BY id DESC;
```
