# Pagatu Frontend — S11 (Angular 22)

Aplicación Angular sin SSR con categorías CRUD, catálogo/carrito, autorización Keycloak PKCE, órdenes, administración de pagos y **Mis pedidos** con polling cada 4 segundos.

## Requisitos (importante: Angular 22)

- Node.js **22.22.3 o superior de la rama 22**, o Node 24.15+ / 26+.
- `npm`, una conexión a Internet en la primera instalación, Java 21 para los servicios Spring.
- Servicios Config (18888), Eureka (18761), Gateway (18080), Catálogo (8080), Cliente (8084), Orden (8082), Pago (8086), Kafka y Keycloak (18180) levantados como en S07b.
- Realm `pagatu`, cliente público `pagatu-frontend`, S256, redirección `http://localhost:4200/auth/callback`.

```powershell
cd clients\pagatu-frontend
node --version
npm install
npm start
```

Visita `http://localhost:4200`. El frontend accede al backend **únicamente a través de `http://localhost:18080`**; la única otra URL es el servidor de identidad `http://localhost:18180`.

Las contraseñas de ejemplo `cliente1` y `admin` pertenecen a **desarrollo local**, no a producción.

## Qué probar

1. Abre Categorías → Keycloak → `cliente1`; visualiza lista, no puedes crear/editar/eliminar.
2. Cierra sesión e inicia como `admin`: crear, editar y borrar una categoría nueva sin productos.
3. Como `cliente1`, crea orden YAPE_PLIN, abre **Mis pedidos** antes de que termine SAGA y observa transición a PAGADA sin recargar.
4. Repite con `TARJETA_RECHAZADA`; debe terminar CANCELADA. Verifica el pago FALLIDO.
5. En la pestaña Network del navegador inspecciona la cabecera CORS, `Authorization` y `X-Trace-ID`.
6. Captura el navegador completo, logs de Orden/Pago, Kafka UI y SQL según guía.

## Seguridad / alcance

- PKCE S256 y `state` verificados. `access_token`, `refresh_token` e `id_token` en memoria; `code_verifier`/`state` en `sessionStorage` solo durante el redirect.
- El frontend **no valida firma JWT**: eso lo hace Gateway y los resource servers.
- **Limitación heredada (S07b/S11):** `GET /api/v1/ordenes` y `GET /api/v1/ordenes/{id}` no verifican propietario en el backend. El filtro de Mis pedidos es solo visual, no control de acceso. `GET /api/v1/pagos/por-orden/{ordenId}` también requiere autorización por propietario antes de despliegue productivo. Pago MS mantiene `permitAll` en su endpoint directo de laboratorio 8086, aunque Gateway aplica roles: no se debe publicar ese puerto. Resolverlo con controles server-side antes de un sistema real.
- La sesión de tokens se pierde al actualizar manualmente el navegador; vuelve a autenticarte. No se usa `localStorage` para tokens ni refresh automático; esto sigue el alcance académico.
