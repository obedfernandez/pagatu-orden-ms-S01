package pe.edu.upeu.cliente.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.cliente.dto.ClienteRequest;
import pe.edu.upeu.cliente.dto.ClienteResponse;
import pe.edu.upeu.cliente.service.ClienteService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService clienteService;

    @GetMapping("/me")
    public ClienteResponse miPerfil(@AuthenticationPrincipal Jwt jwt) {
        Number claimIdCliente = jwt.getClaim("idCliente");
        if (claimIdCliente == null) {
            throw new IllegalArgumentException("Token sin idCliente: no hay un perfil de cliente asociado");
        }
        return clienteService.findById(claimIdCliente.longValue());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse crear(@Valid @RequestBody ClienteRequest request) { return clienteService.crear(request); }

    @GetMapping
    public List<ClienteResponse> listar() { return clienteService.listar(); }

    @GetMapping("/{id}")
    public ClienteResponse findById(@PathVariable Long id) { return clienteService.findById(id); }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return clienteService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { clienteService.eliminar(id); }
}
