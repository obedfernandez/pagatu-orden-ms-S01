package pe.edu.upeu.orden.dto;

import jakarta.validation.constraints.*;
import pe.edu.upeu.orden.domain.EstadoOrden;
import java.math.BigDecimal;

public record OrdenRequest(
        @NotNull @Positive Long clienteId,
        @NotNull @Positive Long conceptoId,
        @NotNull @Positive Integer cantidad,
        @NotNull @DecimalMin(value="0.00") BigDecimal precioUnitario,
        EstadoOrden estado
) {}
