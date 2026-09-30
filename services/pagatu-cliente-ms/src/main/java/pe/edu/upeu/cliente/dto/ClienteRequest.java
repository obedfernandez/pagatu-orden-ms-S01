package pe.edu.upeu.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import pe.edu.upeu.cliente.entity.TipoPersona;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequest {

    @NotNull
    private TipoPersona tipoPersona;

    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
    private String dni;

    @Pattern(regexp = "\\d{11}", message = "El RUC debe tener 11 dígitos")
    private String ruc;

    @Size(max = 150)
    private String nombreCompleto;

    @Size(max = 150)
    private String razonSocial;

    @Size(max = 255)
    private String direccion;

    @Email
    @Size(max = 150)
    private String email;

    @Pattern(regexp = "[0-9+ -]{7,20}", message = "WhatsApp no tiene un formato válido")
    private String whatsapp;
}
