-- Compatibilidad con el evento orden.pagada S08 y la identidad de Keycloak S07b.
-- No borra ni reasigna IDs existentes.
ALTER TABLE notificaciones_pago ALTER COLUMN id_cliente TYPE VARCHAR(36) USING id_cliente::text;
ALTER TABLE clientes ADD COLUMN keycloak_sub VARCHAR(36);
ALTER TABLE clientes ADD CONSTRAINT uk_clientes_keycloak_sub UNIQUE (keycloak_sub);
