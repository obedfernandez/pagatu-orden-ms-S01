package pe.edu.upeu.cliente.event;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrdenPagadaEvento {
    private String tipoEvento;
    private Long ordenId;
    private String idCliente;
    private BigDecimal monto;
    private String canalNotificacion;
    private String origen;
    private Long timestamp;
}
