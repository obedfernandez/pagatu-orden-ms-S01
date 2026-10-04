package pe.edu.upeu.orden.event;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PagoValidadoEvento {
    private String tipoEvento;
    private Long ordenId;
    private BigDecimal monto;
    private String estado;
    private String origen;
    private Long timestamp;
}
