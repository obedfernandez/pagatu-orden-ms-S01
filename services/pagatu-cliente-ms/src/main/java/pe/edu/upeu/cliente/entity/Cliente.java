package pe.edu.upeu.cliente.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_persona", nullable = false, length = 10)
    private TipoPersona tipoPersona;

    @Column(length = 8, unique = true)
    private String dni;

    @Column(length = 11, unique = true)
    private String ruc;

    @Column(name = "nombre_completo", length = 150)
    private String nombreCompleto;

    @Column(name = "razon_social", length = 150)
    private String razonSocial;

    @Column(length = 255)
    private String direccion;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String whatsapp;

    // Vinculo explicito a un usuario de Keycloak. No cambia el ID de negocio.
    @Column(name = "keycloak_sub", length = 36, unique = true)
    private String keycloakSub;
}
