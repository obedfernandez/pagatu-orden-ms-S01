package pe.edu.upeu.cliente.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones_pago")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificacionPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "orden_id", nullable = false, unique = true)
    private Long ordenId;

    @Column(name = "id_cliente", nullable = false, length = 36)
    private String idCliente;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 20)
    private String canal;

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
}
