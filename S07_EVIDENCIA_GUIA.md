# Guía de evidencia S07

Este archivo no reemplaza las capturas reales. La guía de la sesión exige que las capturas se tomen en el equipo del estudiante y muestren fecha/hora y usuario o foto de perfil visibles.

## 1. pagatu-auth-ms

Capturar:

- `POST /api/v1/auth/login` correcto: 200 y `access_token`.
- Login incorrecto: 401 y `Credenciales invalidas`.
- `POST /api/v1/auth/registro`: 201.
- Registro repetido: 409.
- Registro con contraseña corta: 400.
- `GET http://localhost:8085/.well-known/jwks.json` mostrando solo clave pública.
- PostgreSQL con tablas `usuarios`, `roles`, `usuario_roles` y contraseña BCrypt.

## 2. pagatu-gateway como Resource Server

Capturar los cuatro casos:

1. `GET /api/v1/ordenes` sin token → 401.
2. Mismo endpoint con token alterado → 401.
3. `POST /api/v1/productos` con token CLIENTE → 403.
4. Mismo POST con token ADMIN → 201/200.

## 3. pagatu-orden-ms

- Crear orden con token CLIENTE y body sin `idCliente`.
- Mostrar en la respuesta `idCliente: 1`.
- Llamado directo a `http://localhost:8082/api/v1/ordenes` con token inválido → 401.
- Con token ADMIN al crear orden → 400 por falta de `idCliente`.

## 4. pagatu-cliente-ms — actividad autónoma

Matriz implementada:

| Ruta | Método | CLIENTE | ADMIN |
|---|---|---:|---:|
| `/api/v1/clientes/me` | GET | Permitido | Denegado en Gateway |
| `/api/v1/clientes` | GET | 403 | Permitido |
| `/api/v1/clientes/{id}` | GET | 403 | Permitido |
| `/api/v1/clientes/**` | POST/PUT/DELETE | 403 | Permitido |

Capturar:

- CLIENTE llamando `/api/v1/clientes/me` → 200.
- CLIENTE llamando `/api/v1/clientes` → 403.
- ADMIN llamando `/api/v1/clientes` → 200.
- Llamado directo a `http://localhost:8084/api/v1/clientes/me` con token alterado → 401.

## 5. Revisión de accesos y revocación de rol

Consulta:

```sql
SELECT u.email, r.nombre
FROM usuarios u
JOIN usuario_roles ur ON ur.usuario_id = u.id
JOIN roles r ON r.id = ur.rol_id
ORDER BY u.email, r.nombre;
```

Revocar CLIENTE temporalmente:

```sql
DELETE FROM usuario_roles ur
USING usuarios u, roles r
WHERE ur.usuario_id = u.id
  AND ur.rol_id = r.id
  AND u.email = 'cliente@pagatu.com'
  AND r.nombre = 'CLIENTE';
```

Restaurar:

```sql
INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT u.id, r.id
FROM usuarios u, roles r
WHERE u.email = 'cliente@pagatu.com'
  AND r.nombre = 'CLIENTE';
```

Un JWT ya emitido conserva sus roles hasta que expire; para evidenciar la revocación hay que solicitar un token nuevo después de quitar el rol.

## 6. PKCE

Ejecutar `S07_PKCE.ps1` y capturar las tres líneas generadas. El `code_challenge` debe tener 43 caracteres y cambiar completamente al modificar un solo carácter del verifier.
