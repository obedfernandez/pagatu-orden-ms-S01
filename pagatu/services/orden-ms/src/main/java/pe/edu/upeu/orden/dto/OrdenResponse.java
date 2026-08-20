package pe.edu.upeu.orden.dto;

import pe.edu.upeu.orden.domain.EstadoOrden;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdenResponse(Long id, Long clienteId, Long conceptoId, Integer cantidad, BigDecimal precioUnitario, BigDecimal total, EstadoOrden estado, LocalDateTime fechaCreacion) {}
