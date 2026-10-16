# S08 - Contratos de eventos

## `orden.creada`

- Topic: `orden-eventos`
- Productor: `pagatu-orden-ms`
- Consumidor: `pagatu-pago-ms`
- Key: `ordenId`
- Se publica: después de confirmar la transacción de una orden que quedó en `PENDIENTE_PAGO`.

| Campo | Tipo | Significado |
|---|---|---|
| `tipoEvento` | string | Siempre `orden.creada`. |
| `ordenId` | number | Identificador de la orden y key Kafka. |
| `idCliente` | number | Cliente autenticado que creó la orden. |
| `total` | number | Monto calculado por `pagatu-orden-ms`. |
| `metodoPago` | string | `YAPE_PLIN`, `TARJETA` o `PAGO_EFECTIVO`. |
| `origen` | string | Servicio productor. |
| `timestamp` | number | Epoch en milisegundos. |

## `pago.validado`

- Topic: `pago-eventos`
- Productor: `pagatu-pago-ms`
- Consumidor: `pagatu-orden-ms`
- Key: `ordenId`
- Se publica: después de confirmar la transacción que guardó el pago como `VALIDADO`.

| Campo | Tipo | Significado |
|---|---|---|
| `tipoEvento` | string | Siempre `pago.validado`. |
| `ordenId` | number | Orden pagada y key Kafka. |
| `monto` | number | Monto validado. |
| `estado` | string | `VALIDADO`. |
| `origen` | string | Servicio productor. |
| `timestamp` | number | Epoch en milisegundos. |

## Evento autónomo: `orden.pagada`

- Topic: `orden-pagada-eventos`
- Productor: `pagatu-orden-ms`
- Consumidor: `pagatu-cliente-ms`
- Consumer group: `pagatu-cliente-ms-notificaciones`
- Key: `ordenId`
- Se publica: cuando `pagatu-orden-ms` confirma el cambio de estado a `PAGADA`.
- Finalidad: registrar en `pagatu-cliente-ms` una notificación/comprobante simulado, sin llamada HTTP directa entre servicios.

| Campo | Tipo | Significado |
|---|---|---|
| `tipoEvento` | string | Siempre `orden.pagada`. |
| `ordenId` | number | Orden pagada y key Kafka. |
| `idCliente` | number | Cliente al que corresponde el comprobante. |
| `monto` | number | Monto de la orden pagada. |
| `canalNotificacion` | string | Canal simulado; actualmente `WHATSAPP`. |
| `origen` | string | Servicio productor. |
| `timestamp` | number | Epoch en milisegundos. |

El consumidor persiste una fila en `notificaciones_pago` y registra logs con `status=consumed` y `status=processed`.
