# S09 – Implementación sobre S08, registro de cambios

**Base**: ZIP `pagatu-s08-completo.zip`, no el repositorio base del docente. Se
conservaron `orden.pagada`, su consumidor en Cliente, el servicio Python de S08,
infraestructura Kafka y demás avances anteriores.

## Código realizado

1. **Parte A** – `PagoRepository.findByOrdenId` y comprobación previa, más índice
   `UNIQUE` existente. Nueva clase transaccional `PagoPersistenciaService`
   resuelve errores concurrentes después de rollback.
2. **Parte B** – `EstadoPago.FALLIDO`, disparador `TARJETA_RECHAZADA`, publicación
   de `pago.fallido`, consumidor de Orden y transición a `CANCELADA`.
3. **Parte C** – guarda de estado `PENDIENTE_PAGO` y bloqueo pesimista de Orden
   para evitar compensar dos veces, incluso con consumidores concurrentes.
4. **Parte D** – diagrama, contratos, tabla de transiciones y limitaciones en
   `docs/S09_ESTADOS_Y_CONTRATOS.md`.
5. **Parte E (opcional)** – operaciones PATCH de stock en Catálogo, cliente Feign
   Apache HC5 para PATCH, bloqueo de fila, reservas al crear y devoluciones
   al compensar. En caso de carrito inválido intenta revertir lo reservado.
6. **Pruebas automáticas** – suites `S09*Test.java` y `S09_PRUEBAS.ps1`.

## Estado de la validación

El código está implementado y se incluyen pruebas automatizadas **para ejecutar**.
No es correcto declarar el sistema validado de extremo a extremo: en el entorno
usado para preparar este ZIP no había servicios DEV, Docker ni Maven disponible
(localmente el wrapper intenta descargar Maven y no puede conectarse).
Realiza en tu Windows la ejecución de `S09_PRUEBAS.ps1`, `mvnw.cmd test` y las
capturas reales, y adjúntalas al informe. No se han inventado capturas ni logs.

## Actividad autónoma (Proyecto Sello)

La sección 4 de la guía requiere un **proceso propio del Proyecto Sello del equipo**
además de la práctica guiada de Pagatu. No es posible completarlo únicamente
con un ZIP de Pagatu sin el código y dominio del Proyecto Sello. El informe incluye
un checklist y espacio para esa actividad; no se presenta Pagatu como si fuera un
proyecto propio nuevo.

## Pasos inmediatos

Ver `S09_EJECUCION_Y_EVIDENCIAS.md` y `S09_PRUEBAS.ps1`.
