package pe.edu.upeu.cliente.service;

import pe.edu.upeu.cliente.dto.ClienteRequest;
import pe.edu.upeu.cliente.dto.ClienteResponse;

import java.util.List;

public interface ClienteService {
    ClienteResponse crear(ClienteRequest request);
    ClienteResponse actualizar(Long id, ClienteRequest request);
    ClienteResponse findById(Long id);
    ClienteResponse findByKeycloakSub(String sub);
    List<ClienteResponse> listar();
    void eliminar(Long id);
}
