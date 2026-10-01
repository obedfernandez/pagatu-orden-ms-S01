$ErrorActionPreference = "Stop"

Write-Host "=== S07 paso 3.26 - revisión y revocación temporal del rol CLIENTE ==="
Write-Host "1) Revisión actual de accesos"
docker exec -it pagatu-postgres-auth-dev psql -U pagatu -d pagatu_auth_db -c "SELECT u.email, r.nombre AS rol FROM usuarios u JOIN usuario_roles ur ON ur.usuario_id = u.id JOIN roles r ON r.id = ur.rol_id ORDER BY u.email, r.nombre;"

Write-Host ""
Write-Host "Para demostrar la revocación, conserva primero un token CLIENTE ya emitido."
$confirmar = Read-Host "Escribe REVOCAR para quitar temporalmente el rol CLIENTE"
if ($confirmar -ne "REVOCAR") {
    Write-Host "Cancelado. No se modificó la base de datos."
    exit 0
}

docker exec -it pagatu-postgres-auth-dev psql -U pagatu -d pagatu_auth_db -c "DELETE FROM usuario_roles WHERE usuario_id = (SELECT id FROM usuarios WHERE email = 'cliente@pagatu.com');"
Write-Host "Rol revocado. El token viejo seguirá funcionando hasta expirar; un token nuevo ya no tendrá CLIENTE."

$restaurar = Read-Host "Escribe RESTAURAR para devolver el rol CLIENTE ahora"
if ($restaurar -eq "RESTAURAR") {
    docker exec -it pagatu-postgres-auth-dev psql -U pagatu -d pagatu_auth_db -c "INSERT INTO usuario_roles (usuario_id, rol_id) SELECT u.id, r.id FROM usuarios u, roles r WHERE u.email = 'cliente@pagatu.com' AND r.nombre = 'CLIENTE' ON CONFLICT DO NOTHING;"
    Write-Host "Rol CLIENTE restaurado."
    docker exec -it pagatu-postgres-auth-dev psql -U pagatu -d pagatu_auth_db -c "SELECT u.email, r.nombre AS rol FROM usuarios u JOIN usuario_roles ur ON ur.usuario_id = u.id JOIN roles r ON r.id = ur.rol_id ORDER BY u.email, r.nombre;"
} else {
    Write-Warning "El rol quedó revocado. Ejecuta el INSERT indicado en S07_EVIDENCIA_GUIA.md antes de terminar."
}
