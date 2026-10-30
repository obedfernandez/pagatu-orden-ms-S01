# OBS S10 — Pagatu, paquete preparado (29/10/2026)

Esta entrega se basó en la rama **s11-angular-keycloak** del ZIP del titular. No contiene capturas fabricadas, ni simula resultados locales. Ningun contenedor del computador del titular fue iniciado desde este entorno.

## ¿Qué quedó preparado?

- `grafana/dashboards/PAGATU_OBS_S10.json`: 9 paneles reales mediante Prometheus y Loki. No utiliza valores fijos: los resultados dependen de tu Pagatu vivo.
- `INSTALAR_DASHBOARD_S10.ps1`: importación vía API Grafana 11, descubre UIDs reales de data sources; si ya creaste el dashboard con el mismo título, **guarda respaldo de ese dashboard y lo actualiza**. No reconstruye ni reinicia Docker.
- `VERIFICAR_OBS_S10.ps1`: comprobaciones read-only y consulta `up`, permite guardar JSON de evidencia.
- `GENERAR_TRAZA_LECTURA_S10.ps1`: emite GET a Catálogo con `X-Trace-ID` nuevo. GET de Órdenes opcional con un JWT autorizado. Nunca crea ni paga una orden.
- Catálogo y Órdenes: log estructurado de cada llamada a `/api/` en `CorrelationIdFilter` con `traceId`, método, ruta y estado, sin loguear tokens.
- Órdenes: `TraceIdFeignConfig` propaga el MDC `traceId` al HTTP Feign dirigido a Catálogo (en el mismo hilo); incluye una prueba JUnit. **Esto exige recompilar y reiniciar solo esos microservicios cuando vayas a demostrar una llamada entre ellos**.
- Config Server DEV para Catálogo y Órdenes: `/actuator/health/readiness` incorpora `readinessState,db`, para demostrar la dependencia PostgreSQL. **Exige reiniciar esos microservicios para tomar la configuración nueva**.

## PASO A: Instalar dashboard sin tocar tus contenedores

1. **Haz respaldo de la carpeta Pagatu local** o utiliza Git para saber qué archivos modificar. Extrae este paquete de actualización sobre la raíz de tu proyecto `pagatu`, conservando rutas relativas `obs/`, `infra/`, `services/`. Si tu copia local tiene cambios posteriores a GitHub, compáralos antes: el ZIP adjunto parte de la rama S11 descargada.
2. En la PowerShell abierta en `C:\Users\emerl\OneDrive\Documentos\distribuidas\pagatu` ejecuta:

   ```powershell
   .\obs\INSTALAR_DASHBOARD_S10.ps1
   ```

   Introduce en el diálogo las credenciales reales de Grafana DEV. Por defecto según tu compose-dev: `admin`/`admin`, salvo que cambiaste la contraseña.
3. Abre `http://localhost:13000/dashboards` y entra en `PAGATU — Observabilidad y Diagnóstico S10`. Los primeros paneles deben mostrar `UP` y las métricas JVM que existan. Los de peticiones sin actividad pueden mostrar cero o `No data`; no inventar datos.
4. `grafana/respaldos-dashboard` guarda los JSON anteriores si el título ya existía. **No ejecutes `docker compose down -v` ni recrees Grafana**: su `compose-dev.yml` original no monta un volumen persistente para la DB de dashboards. Puedes volver a ejecutar el importador si pierdes el dashboard tras recrearlo.

## PASO B: Verificar salud y obtener evidencia

```powershell
.\obs\VERIFICAR_OBS_S10.ps1 -Evidencias .\obs\evidencias-locales
```

Hay que tener Config 18888, Eureka 18761, Catálogo 8080, PostgreSQL 15432; para Orden deben estar su PostgreSQL 15434, Kafka 19092 y Keycloak 18180 con el realm `pagatu` (y Orden ejecutándose en 8082). Si no existe una dependencia, el script informa `NO VERIFICADO`: **no significa que la hayas probado**. Las 3/3 métricas UP previas eran Config/Eureka/Catálogo; Ordén no estaba iniciado en esas capturas.

## PASO C: Logs reales / correlación

Los servicios `catalogo` y `orden` ya generan archivos `logs/*.log` según `logback-spring.xml` del ZIP. Promtail lee los directorios de ambos desde Docker. Para comprobar:

```powershell
.\obs\GENERAR_TRAZA_LECTURA_S10.ps1
```

En Grafana: **Explore → Loki**, consulta:

```logql
{application=~"pagatu-(catalogo|orden)-ms"} |= "obs_http"
```

Si Orden está en ejecución, puedes pasar un JWT de una cuenta sintética mediante `-TokenOrden` (no incluir el token en capturas). Dos GET distintos con el mismo ID prueban que el *identificador* llega a ambos destinos, **pero NO prueban propagación automática entre servicios**. La propagación Feign se prueba con una llamada real de prueba `Orden -> Catalogo` en un laboratorio de datos sintéticos, después de reiniciar el código Orden nuevo. No hagas una compra real sobre registros históricos para crear esa evidencia. Los eventos Kafka de este proyecto **no** transportan automáticamente el MDC traceId: no afirmarlo.

## PASO D: Fallo controlado / recuperación, solo si lo autorizas

En esta rama, la configuración `eureka_sd_configs` hace que una instancia detenida pueda **desaparecer** de los targets en vez de quedar como `DOWN`: eso no prueba un falso negativo. Para la demostración segura sugerida:

1. En el laboratorio DEV donde Catálogo fue lanzado con Maven, comprueba que no hay operaciones en curso ni otras personas usándolo.
2. Registra health/readiness y `up` iniciales; toma captura con fecha/hora.
3. Detén **solo el proceso de Catálogo** desde su terminal Maven usando `Ctrl+C`. No detengas PostgreSQL, no borres contenedores ni volúmenes.
4. Comprueba `http://localhost:8080/actuator/health`, la disponibilidad en Eureka, los paneles y el último error de la consulta read-only. En Eureka la salida puede demorar; el panel debe reflejar el descubrimiento sin inventar estados.
5. Vuelve a iniciarlo desde `services/pagatu-catalogo-ms` con `.\mvnw.cmd spring-boot:run`. Espera que vuelva a responder y registra la recuperación.

La dependencia PostgreSQL se demuestra con los detalles `/actuator/health/readiness` y su estado `db`; no se recomienda detener el PostgreSQL del laboratorio con datos existentes. Los scripts no ejecutan fallas destructivas.

## Lista de capturas para la tarea

1. Terminal con health/readiness y `db: UP` donde el endpoint esté habilitado, sin datos privados.
2. Eureka mostrando Catálogo y, cuando esté ejecutándose, Órdenes.
3. Prometheus `/targets` con Config/Eureka/Catálogo (y Órdenes cuando corresponda).
4. Grafana con 3 o más paneles reales (targets, tráfico, JVM).
5. Grafana Explore Loki con logs `obs_http` de Catálogo y Orden y traza correlacionada real si se ejecutó.
6. Pantallas de la falla controlada / recuperación en DEV, con contexto horario; no inventar capturas.
7. Reflexión sobre prevención, detección, diagnóstico y recuperación; incluir límites reales.

## Límites de lo verificado

- **Sí verificado aquí:** integridad JSON, sintaxis YAML/XML y referencias entre dashboard/scripts; comparación contra el código de la rama S11.
- **No verificado aquí:** ejecución Docker/PowerShell en Windows del usuario, API Grafana local, compilación Java con dependencias del proyecto, log correlacionado en runtime, ni caída/recovery reales.
- El envío HTTP Grafana usa el API legado `/api/dashboards/db`, correcto para Grafana 11.4.0 del compose DEV. Se guardan respaldos antes de sobrescribir el dashboard existente.
- Las credenciales y contraseñas DEV del repositorio son de demostración, no para producción. Nunca publiques capturas con JWT, secretos, información de clientes u órdenes reales.
