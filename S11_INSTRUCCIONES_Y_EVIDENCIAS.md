# Pagatu S11 — Integración Angular 22 + Keycloak

**Base:** implementación S07b Keycloak, OAuth 2.0 con PKCE, UUID y SAGA S09.
**Estado:** código fuente preparado; pruebas end-to-end reales pendientes de ejecutarse en Windows con Docker, Kafka y Keycloak.
**Guía:** https://262dist.github.io/pagatu/sesiones/S11_Integracion_Cliente_Frontend_Angular_Keycloak/

## Cambios en el backend

- `infra/pagatu-gateway/.../CorsConfig.java`: CORS centralizado en Spring Cloud Gateway WebMVC. Origen Angular 4200, Authorization, X-Trace-ID y preflight OPTIONS con Spring Security `.cors(...)`.
- `infra/pagatu-config/config-repo/pagatu-gateway-{dev,prod}.yml`: `pagatu.cors.allowed-origins` local (variable `PAGATU_FRONTEND_ORIGIN` para cambiarlo).
- `services/pagatu-catalogo-ms/CorsConfig.java`: demo opcional del paso 3.2; **desactivada** mediante `pagatu.cors.enabled=false` en catálogo-dev, para evitar doble header.
- `pagatu-pago-ms`: nuevo `GET /api/v1/pagos/por-orden/{ordenId}` (404 normal hasta que haya pago).
- `Gateway`: `GET /api/v1/pagos` reservado a ADMIN; GET /api/v1/pagos/** para autenticados CLIENTE o ADMIN.

## Verificación del frontend (3.1–3.8)

1. Verifica Docker, Java, Keycloak, Eureka, Kafka, Config y Gateway sin arrancar `pagatu-auth-ms`.
2. Comprueba `node --version`: **Angular 22 requiere >=22.22.3** en la rama 22 (Node 24.15+ también sirve). Si la versión es menor, actualiza desde nodejs.org.
3. Abre el repositorio en VS Code. En la terminal: `cd clients/pagatu-frontend; npm install; npm start`.
4. Abre `http://localhost:4200` y selecciona Categorías. Inicia sesión en Keycloak como `cliente1` / `cliente123` para consultar; como `admin` / `admin123` para CRUD. Esas credenciales son solo de laboratorio.
5. `Nueva orden` → producto activo con stock → método `YAPE_PLIN` → Confirmar. El texto debe informar `PENDIENTE_PAGO` mientras Kafka trabaja; `Mis pedidos` realiza sondeo automático hasta PAGADA.
6. Repite con `TARJETA_RECHAZADA`, confirma CANCELADA y pago FALLIDO sin recargar. Recuerda que consume/restaura stock durante la prueba.
7. Admin → Pagos; CLIENTE no puede abrir el listado completo.
8. Captura evidencia sin recortar con hora y usuario visibles: login, cabeceras CORS, categorías, orden pendiente, pagada, cancelada, logs y Kafka UI.

## Archivos para el informe

- captura de navegador de inicio de sesión Keycloak y Network (code_challenge_method=S256; no fotografiar tokens completos).
- captura de Categorías en Angular.
- captura de Mis pedidos cambiando a PAGADA y CANCELADA.
- captura de endpoint `/api/v1/pagos/por-orden/{id}` en Network.
- logs o SQL de órdenes y pagos sin publicar contraseñas.

## Rama Git

Aplicar el parche sobre tu clon con historial Git. Desde `pagatu-s07b-con-git`:
```powershell
git switch s07b-keycloak
git switch -c s11-angular-keycloak
# Copia los archivos del parche (no sobrescribir .git ni ejecutar /MIR)
git status --short
git add -- clients/ infra/pagatu-gateway/ infra/pagatu-config/config-repo/ services/pagatu-catalogo-ms/src/main/java/pe/edu/upeu/catalogo/CorsConfig.java services/pagatu-pago-ms/ S11_INSTRUCCIONES_Y_EVIDENCIAS.md S07B_PKCE.ps1 S07B_PRUEBAS.ps1
git diff --cached --check
git commit -m "feat: integrar Angular 22 Keycloak y consistencia eventual S11"
git push -u origin s11-angular-keycloak
```
Antes de publicar revisar el diff completo y excluir secretos personales.

## Límite conocido

El propio guion S11 reconoce que filtrar las órdenes por UUID en la SPA no es un control de acceso: la API histórica lista órdenes de todos. La solución real es autorizar acceso por propietario (o rol ADMIN) en Orden MS y Pago MS; está fuera del cambio mínimo pedido en S11, pero es obligatorio para producción.
