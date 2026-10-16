package pe.edu.upeu.orden.event;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrdenCreadaEvento {
    private String tipoEvento;
    private Long ordenId;
    private String idCliente;
    private BigDecimal total;
    private String metodoPago;
    private String origen;
    private Long timestamp;
}
