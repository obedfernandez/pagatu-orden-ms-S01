# S09 – Guía de ejecución en Windows/PowerShell y evidencias

## Antes de iniciar

- Java 21, Docker Desktop y VS Code.
- Levantar Config (`18888`), Eureka (`18761`), Gateway (`18080`), Auth (`8085`),
  Catálogo (`8080`), Orden (`8082` según DEV), Pago (`8086`) y Cliente, además de
  las bases respectivas y Kafka (`19092`). Reutiliza los pasos de `S08_PASOS_EJECUCION.md`.
- No modificar ni borrar los volúmenes PostgreSQL existentes, pues contienen
  datos de ejercicios anteriores.
- Desde raíz del proyecto, si faltan contenedores:

```powershell
docker compose -f kafka/compose-dev.yml up -d
docker compose -f services/pagatu-pago-ms/compose-dev.yml up -d
# Para las demás bases, seguir compose-dev.yml de cada servicio si es necesario.
```

## Compilar cambios: desde cada microservicio

```powershell
cd services/pagatu-catalogo-ms
.\mvnw.cmd clean test
cd ..\pagatu-pago-ms
.\mvnw.cmd clean test
cd ..\pagatu-orden-ms
.\mvnw.cmd clean test
```

Para pruebas unitarias focalizadas, en Pago: `./mvnw.cmd -Dtest=S09PagoServiceTest test`;
en Orden: `./mvnw.cmd -Dtest=S09* test`; en Catálogo: `./mvnw.cmd -Dtest=S09StockTest test`.

Para arrancar desde cada carpeta: `.\mvnw.cmd spring-boot:run` (luego levantar
el siguiente en otra terminal). Reiniciar Orden, Pago y Catálogo tras cambios.

## Prueba automática de S09

Desde la **raíz del proyecto**, en nueva PowerShell y con todos los servicios
operativos:

```powershell
.\S09_PRUEBAS.ps1
```

El script inicia sesión con CLIENTE de desarrollo, crea una orden pagada,
reenvía `orden.creada` por `pagatu-kafka-dev`, comprueba que solo existe un pago,
crea una orden con `TARJETA_RECHAZADA`, espera `CANCELADA`, valida que el stock
se restauró, repite `pago.fallido`, y comprueba que no hay doble devolución.
Si falla, no presenta una salida exitosa falsa: arroja un error que debe investigarse.

**Atención**: el script usa la cuenta y contraseña de demostración de la guía;
no se deben usar en producción. Necesita el contenedor `pagatu-kafka-dev`
para producir los mensajes duplicados. La verificación de stock opcional asume
que ninguna otra prueba modifica simultáneamente el producto elegido.

## Evidencias reales para el docente (no generadas de antemano)

| Prueba | Qué capturar en tu pantalla | Resultado buscado |
|---|---|---|
| 3.1 S08 sano | GET de la orden y log `pago.validado` | `PAGADA` |
| 3.2 bug ANTES del cambio | ejecutar la versión antigua S08 y reenviar el mensaje | excepción UNIQUE verdadera |
| 3.4 S09 idempotente | Kafka UI, log Pago, consulta SQL | `status=ignored`, un pago |
| 3.7 falla el pago | log Pago + Orden, JSON GET Pago/Orden | `FALLIDO`, `CANCELADA` |
| 3.8 duplicar compensación | Kafka UI, log Orden, GET posterior | `status=ignored`, `CANCELADA` |
| 3.13 opcional | capturas de stock antes / reservado / repuesto | stock original restaurado |
| 4 Actividad autónoma | 3 casos con proceso del Proyecto Sello | evidencias de 2 microservicios propios |

**IMPORTANTE:** Para la prueba del bug previo (3.2), no deshagas los arreglos
S09 sobre tus datos actuales: usa por separado una copia/ramal S08, guarda
los logs reales, luego vuelve a este proyecto S09. Tu PDF debe incluir capturas
**sin recortar** con reloj y usuario visibles, como pide la guía.

Consulta de BD para un ID real:

```powershell
docker exec pagatu-postgres-pago-dev psql -U pagatu -d pagatu_pago_db -c 'SELECT orden_id, COUNT(*) FROM pagos GROUP BY orden_id ORDER BY orden_id DESC LIMIT 20;'
```

En Kafka UI (http://localhost:18085) usa `orden-eventos`, `pago-eventos` y
`orden-pagada-eventos`. El tercero pertenece a tu trabajo autónomo de S08.

## Informe de entrega

El docente pide **PDF** `S09_Equipo##_ApellidoNombre.pdf` con evidencia del
bug antes/después, camino fallido, compensación idempotente, actividad autónoma,
error diagnosticado, reflexión de 5–8 líneas, feedback y enlace GitHub.
En `S09_PLANTILLA_INFORME.md` se incluye una plantilla honesta. No generes
capturas sintéticas; obtén las evidencias de tu PC tras levantar el sistema.
