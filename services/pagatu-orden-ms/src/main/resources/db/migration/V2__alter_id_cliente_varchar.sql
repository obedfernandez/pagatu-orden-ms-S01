-- Mantiene las ordenes antiguas: el numero anterior se convierte a texto.
-- Los nuevos pedidos guardan el subject (UUID) de Keycloak.
ALTER TABLE ordenes ALTER COLUMN id_cliente TYPE VARCHAR(36) USING id_cliente::text;
