package pe.edu.upeu.orden.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Orden {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="cliente_id", nullable=false) private Long clienteId;
    @Column(name="concepto_id", nullable=false) private Long conceptoId;
    @Column(nullable=false) private Integer cantidad;
    @Column(name="precio_unitario", nullable=false, precision=12, scale=2) private BigDecimal precioUnitario;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal total;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=30) private EstadoOrden estado;
    @Column(name="fecha_creacion", nullable=false) private LocalDateTime fechaCreacion;

    @PrePersist
    void prePersist() {
        if (estado == null) estado = EstadoOrden.CREADA;
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
    }
}
