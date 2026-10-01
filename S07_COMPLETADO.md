# S07 completado — Seguridad distribuida y control de acceso

Este proyecto parte de S06 y agrega toda la implementación técnica de S07, incluyendo la actividad autónoma de `pagatu-cliente-ms`.

## Implementado

- `pagatu-auth-ms` con PostgreSQL propio (`pagatu_auth_db`), Flyway, usuarios, roles y `usuario_roles`.
- Usuarios semilla: `admin@pagatu.com / admin123` y `cliente@pagatu.com / cliente123`.
- Login y registro, contraseñas BCrypt, JWT RS256, `realm_access.roles`, `idCliente` y JWKS público.
- `pagatu-gateway` como Resource Server con RBAC para catálogo, órdenes y clientes.
- `pagatu-orden-ms` como Resource Server; `idCliente` ya no viene en `OrdenRequest`, se obtiene del JWT validado.
- `pagatu-cliente-ms` como Resource Server y endpoint `GET /api/v1/clientes/me` basado en el `idCliente` del JWT.
- Configuración DEV y PROD preparada para Auth/Gateway/Orden/Cliente.
- `OpenApiConfig` en `pagatu-orden-ms` (y también en `pagatu-cliente-ms`) para probar endpoints protegidos con el botón **Authorize** de Swagger.
- Script `S07_PRUEBAS.ps1` para obtener tokens y ejecutar los casos principales de 401/403/éxito.
- Script `S07_REVOCACION_ROL.ps1` para el paso 3.26 (revisión y revocación/restauración temporal del rol).
- Script `S07_PKCE.ps1` para el paso 3.27 (code verifier/challenge PKCE).
- `S07_EVIDENCIA_GUIA.md` con la lista de capturas reales que exige la entrega.

## Matriz de roles y accesos

| Ruta | Método | Público | CLIENTE | ADMIN |
|---|---|---:|---:|---:|
| `/api/v1/auth/login` | POST | Sí | Sí | Sí |
| `/api/v1/auth/registro` | POST | Sí | Sí | Sí |
| `/api/v1/productos/**` y `/api/v1/categorias/**` | GET | Sí | Sí | Sí |
| `/api/v1/productos/**` y `/api/v1/categorias/**` | POST/PUT/DELETE | No | No (403) | Sí |
| `/api/v1/ordenes/**` | Todos | No | Sí | Sí* |
| `/api/v1/clientes/me` | GET | No | Sí | No |
| `/api/v1/clientes` y `/api/v1/clientes/{id}` | GET | No | No | Sí |
| `/api/v1/clientes/**` | POST/PUT/DELETE | No | No | Sí |

\* Un ADMIN puede pasar el Gateway hacia órdenes, pero al crear una orden recibe 400 si su JWT no contiene `idCliente`, tal como plantea S07.

## Orden recomendado de arranque en DEV

1. PostgreSQL de catálogo, cliente, orden y auth (`compose-dev.yml` de cada servicio).
2. `infra/pagatu-config`.
3. `infra/pagatu-eureka`.
4. `services/pagatu-auth-ms`.
5. `services/pagatu-catalogo-ms`.
6. `services/pagatu-cliente-ms`.
7. `services/pagatu-orden-ms`.
8. `infra/pagatu-gateway`.

Después ejecuta `./S07_PRUEBAS.ps1` desde PowerShell (en Windows: `powershell -ExecutionPolicy Bypass -File .\S07_PRUEBAS.ps1`). Para los pasos finales de la guía también están `S07_REVOCACION_ROL.ps1` y `S07_PKCE.ps1`.

## Evidencia para el informe

Las capturas reales deben tomarse en tu equipo con reloj y usuario visibles. El código deja preparados los escenarios que pide la guía: login 200, login inválido 401, registro 201/409/400, JWKS, Gateway 401/401/403/201, orden con `idCliente` del JWT, orden directa con token inválido 401 y cliente permitido/denegado/directo 401.

## Rama Git preparada

El proyecto queda sobre la rama `s07-seguridad-jwt`. Cuando termines de ejecutar las pruebas y revisar tus evidencias, puedes registrar el avance con:

```powershell
git add .
git commit -m "feat: completar S07 seguridad JWT y control de acceso"
git push -u origin s07-seguridad-jwt
```

Antes del `push`, usa `git status` para confirmar exactamente qué archivos vas a subir.
