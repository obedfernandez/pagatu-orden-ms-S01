package pe.edu.upeu.cliente.dto;

import lombok.*;
import pe.edu.upeu.cliente.entity.TipoPersona;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {
    private Long id;
    private TipoPersona tipoPersona;
    private String dni;
    private String ruc;
    private String nombreCompleto;
    private String razonSocial;
    private String direccion;
    private String email;
    private String whatsapp;
}
