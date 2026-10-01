# Validación técnica previa de S07

## Comprobaciones realizadas sobre el proyecto

- Los YAML modificados de Auth, Gateway, Orden y Cliente son sintácticamente válidos.
- Los `pom.xml` modificados y `logback-spring.xml` son XML válidos.
- El código nuevo/modificado pasó una revisión de sintaxis Java con JDK 21; en este entorno no están descargadas las dependencias Maven, por lo que una compilación completa no puede resolverse aquí.
- `pagatu-auth-ms` contiene la migración Flyway, usuarios/roles semilla, BCrypt, JWT RS256 y JWKS público.
- Gateway, Orden y Cliente tienen la dependencia de OAuth2 Resource Server y `jwk-set-uri` para DEV/PROD.
- `OrdenRequest` ya no contiene `idCliente`; `OrdenController` lo obtiene del claim `idCliente` del JWT.
- `pagatu-cliente-ms` valida JWT y agrega `GET /api/v1/clientes/me` usando el claim `idCliente`.
- `pagatu-orden-ms` y `pagatu-cliente-ms` tienen configuración OpenAPI Bearer para probar desde Swagger.
- Se incluyen scripts para los casos de prueba de S07, revocación de rol (3.26) y PKCE (3.27).

## Qué debes validar en tu PC antes de entregar

La guía exige evidencia real de ejecución. Arranca Docker y los servicios en el orden indicado en `S07_COMPLETADO.md`, ejecuta `S07_PRUEBAS.ps1`, luego `S07_REVOCACION_ROL.ps1` y `S07_PKCE.ps1`, y toma las capturas indicadas en `S07_EVIDENCIA_GUIA.md`.

No se incluyen capturas simuladas ni resultados inventados: los códigos HTTP y las tablas deben salir de tu propia ejecución para que la evidencia sea válida.
