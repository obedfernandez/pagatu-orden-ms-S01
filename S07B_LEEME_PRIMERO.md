# S07b — Pagatu completo con Keycloak (integracion sobre el ZIP S09)

**Base:** copia del proyecto S09 proporcionado por el estudiante, con servicios, infraestructura, mensajeria y documentacion S08/S09.
**Estado:** codigo y migraciones preparados; **NO se han ejecutado las pruebas de extremo a extremo en la laptop del estudiante**. No confundir con S09 que si fue probado anteriormente.

Guia docente: https://262dist.github.io/pagatu/sesiones/S07b_Seguridad_Distribuida_Keycloak/

## Cambios incluidos

- `keycloak/compose-dev.yml` y `keycloak/realm-pagatu.json`: contenedores Keycloak 26.8.0 + PostgreSQL 17, import automatico al arranque del realm `pagatu`, roles CLIENTE/ADMIN, client `pagatu-frontend` publico con Authorization Code + PKCE S256 y usuarios de practica.
- Gateway, Orden MS, Cliente MS y Pago MS: `issuer-uri` de Keycloak en configuraciones DEV/PROD; Gateway deja de anunciar la ruta de login del antiguo Auth.
- Orden MS: `idCliente` cambia a `String` y se extrae del `sub` del JWT. Flyway `V2` cambia `ordenes.id_cliente` a `VARCHAR(36)` conservando pedidos anteriores como texto.
- Eventos S08 y S09: `idCliente` en ambos extremos de Kafka y notificaciones pasa a `String` para no romper la deserializacion.
- Cliente MS: Flyway `V4` convierte `notificaciones_pago.id_cliente` a `VARCHAR(36)` y crea `clientes.keycloak_sub` opcional, con indice UNIQUE. `/api/v1/clientes/me` identifica por `sub` **solo si un perfil fue vinculado expresamente**. No mezcla una identidad UUID nueva con el identificador numerico de un perfil ajeno.
- Scripts de backup, token PKCE, pruebas de roles, pruebas S09 adaptadas y vinculacion administrativa de perfil.
- Se conserva el codigo historico `pagatu-auth-ms` por referencia; NO se arranca tras la migracion. Tampoco se borran contenedores ni volumenes.

## Recomendacion para tu laptop

Este ZIP es una **copia independiente** para abrir con VS Code. No copies encima de tu carpeta S09 abierta mientras estan corriendo los servicios. Si quieres conservar el historial Git de tu repo, crea una rama `s07b-keycloak` desde `s09-consistencia-saga` en tu repositorio y copia los cambios S07b desde esta copia (no copies carpetas `.git` ni `target`). El ZIP **no incluye `.git`** ni contrasenas personales de tu equipo.

1. **Deten primero** con Ctrl+C los servidores antiguos `pagatu-auth-ms`, `pagatu-orden-ms`, `pagatu-pago-ms`, `pagatu-cliente-ms` y `pagatu-gateway` para no combinar versiones. Puedes dejar Kafka, los Postgres y Config/Eureka levantados. **No ejecutes `docker compose down -v`.**
2. En PowerShell, desde la raiz de esta copia, crea los respaldos del cliente y orden: `powershell -NoProfile -ExecutionPolicy Bypass -File .\S07B_RESPALDAR_BD.ps1` (requiere que ambos contenedores Postgres esten levantados). Los guarda en `respaldos-s07b/`, ignorado por Git.
3. Levanta Keycloak: `docker compose -f .\keycloak\compose-dev.yml up -d`. Espera que responda: http://localhost:18180/realms/pagatu/.well-known/openid-configuration. Admin: http://localhost:18180 (`admin`/`admin123`, **solo en LOCAL**). La primera importacion del realm ocurre al iniciar con BD vacia: **si el realm ya existe, Keycloak no sobreescribe usuarios o clientes anteriores**.
4. Realm `pagatu`: verifica usuarios `cliente1` / `cliente123` y `admin` / `admin123`. El cliente `pagatu-frontend` exige PKCE S256 y **tiene Direct Access Grants desactivado**. Si algun rol no aparece en el access token bajo `realm_access.roles`, revisa el client scope **roles** en la consola de Keycloak antes de continuar.
5. **Deten Cliente y Orden antes de Flyway**, no edites SQL con DevTools arrancado. Inicia (o reinicia) **Config Server** antes de Gateway y microservicios. Inicia los servicios Gateway, Catálogo, Cliente, Orden, Pago desde sus carpetas con `.\mvnw.cmd spring-boot:run` como en S09. Cliente aplicara `V4` y Orden `V2`; valida `\d ordenes` y `\d notificaciones_pago` en psql para ver `VARCHAR(36)`.
6. Desde la raiz: `. .\S07B_PKCE.ps1` y `$tokenCliente = Get-PagatuKeycloakToken -Cuenta CLIENTE`. El navegador finaliza en `http://localhost:4200/auth/callback?code=...` que aun no existe; **copia la URL completa en la misma terminal** cuando el script lo pida. El token permanece en memoria, no se guarda en un archivo. Repite para ADMIN si hace falta.
7. Ejecuta `.\S07B_PRUEBAS.ps1` para 401/403/200 sin insertar datos; agrega `-ProbarAdmin` para probar rol ADMIN, `-CrearOrden` para crear una orden real (consume stock) o `-ProbarAdmin -CrearProductoAdmin` para comprobar el 201 de ADMIN (crea un producto). Para repetir SAGA ejecuta `.\S07B_S09_PRUEBAS.ps1`, **no el script S09 original**, que era para Auth propio.
8. `GET /api/v1/clientes/me` responde 404 mientras `clientes.keycloak_sub` no este vinculado. Si deseas recuperar el perfil de negocio original, identifica el UUID del nuevo usuario y vincula de forma consciente, por ejemplo ` .\S07B_VINCULAR_CLIENTE.ps1 -ClienteId 1 -Sub <UUID_VALIDO>`; requiere un perfil real y la migracion V4 aplicada. No asignar el perfil ajeno.

## Compatibilidad y limitaciones

- La guia orienta DEV a `http://localhost:18180/realms/pagatu` y PROD-local a `http://pagatu-keycloak:8080/realms/pagatu`; para contenedores, **la URL del issuer del JWT debe coincidir EXACTAMENTE** con lo que validan los resource servers. No se ha probado PROD dockerizado. Ajusta el hostname/issuer unificado antes de desplegar PROD; `start-dev`, credenciales semilla y puertos expuestos **no son configuracion de produccion segura**.
- Los IDs numericos guardados antes de S07b permanecen como texto (`"1"`, `"2"`): no equivalen automaticamente a `sub` UUID de los usuarios nuevos. La migracion de cuentas viejas a nuevas exige mapeo verificado.
- Las pruebas automatizadas existentes `S09_PRUEBAS.ps1` y el login de Auth solo representan S09 historico. La version S07b del script usa PKCE.
- La importacion automatica no reinicia ni elimina la BD Auth antigua; mantenla de respaldo mientras pruebas. El codigo de Auth sigue como referencia de S07.
- **Sin garantia de ejecucion verificada**: Docker, Keycloak y Maven no se han ejecutado contra tus servicios reales desde este entorno. Comprobar healthchecks, migraciones y SAGA antes de publicarlo.
