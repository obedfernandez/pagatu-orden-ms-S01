CREATE TABLE ordenes (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    concepto_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(12,2) NOT NULL CHECK (precio_unitario >= 0),
    total NUMERIC(12,2) NOT NULL CHECK (total >= 0),
    estado VARCHAR(30) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ordenes_cliente_id ON ordenes(cliente_id);
CREATE INDEX idx_ordenes_estado ON ordenes(estado);
