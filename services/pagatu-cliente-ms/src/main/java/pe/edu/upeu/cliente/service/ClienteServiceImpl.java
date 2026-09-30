package pe.edu.upeu.cliente.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.cliente.dto.ClienteRequest;
import pe.edu.upeu.cliente.dto.ClienteResponse;
import pe.edu.upeu.cliente.entity.Cliente;
import pe.edu.upeu.cliente.entity.TipoPersona;
import pe.edu.upeu.cliente.exception.ResourceNotFoundException;
import pe.edu.upeu.cliente.mapper.ClienteMapper;
import pe.edu.upeu.cliente.repository.ClienteRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        validarReglas(request, null);
        Cliente cliente = clienteMapper.toEntity(request);
        normalizar(cliente);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
        validarReglas(request, id);
        clienteMapper.updateEntity(request, cliente);
        normalizar(cliente);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse findById(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
        return clienteMapper.toResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream()
                .map(clienteMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente no encontrado: " + id);
        }
        clienteRepository.deleteById(id);
    }

    private void validarReglas(ClienteRequest request, Long idActual) {
        String dni = limpiar(request.getDni());
        String ruc = limpiar(request.getRuc());
        String nombreCompleto = limpiar(request.getNombreCompleto());
        String razonSocial = limpiar(request.getRazonSocial());

        if (request.getTipoPersona() == TipoPersona.NATURAL) {
            if (dni == null) {
                throw new IllegalArgumentException("Una persona NATURAL debe registrar DNI");
            }
            if (nombreCompleto == null) {
                throw new IllegalArgumentException("Una persona NATURAL debe registrar nombreCompleto");
            }
        } else if (request.getTipoPersona() == TipoPersona.JURIDICA) {
            if (ruc == null) {
                throw new IllegalArgumentException("Una persona JURIDICA debe registrar RUC");
            }
            if (razonSocial == null) {
                throw new IllegalArgumentException("Una persona JURIDICA debe registrar razonSocial");
            }
            if (dni != null) {
                throw new IllegalArgumentException("Una persona JURIDICA no debe registrar DNI propio");
            }
        }

        if (dni != null) {
            boolean duplicado = idActual == null
                    ? clienteRepository.existsByDni(dni)
                    : clienteRepository.existsByDniAndIdNot(dni, idActual);
            if (duplicado) {
                throw new IllegalArgumentException("El DNI ya está registrado: " + dni);
            }
        }

        if (ruc != null) {
            boolean duplicado = idActual == null
                    ? clienteRepository.existsByRuc(ruc)
                    : clienteRepository.existsByRucAndIdNot(ruc, idActual);
            if (duplicado) {
                throw new IllegalArgumentException("El RUC ya está registrado: " + ruc);
            }
        }
    }

    private void normalizar(Cliente cliente) {
        cliente.setDni(limpiar(cliente.getDni()));
        cliente.setRuc(limpiar(cliente.getRuc()));
        cliente.setNombreCompleto(limpiar(cliente.getNombreCompleto()));
        cliente.setRazonSocial(limpiar(cliente.getRazonSocial()));
        cliente.setDireccion(limpiar(cliente.getDireccion()));
        cliente.setEmail(limpiar(cliente.getEmail()));
        cliente.setWhatsapp(limpiar(cliente.getWhatsapp()));

        if (cliente.getTipoPersona() == TipoPersona.NATURAL) {
            cliente.setRazonSocial(null);
        } else {
            cliente.setDni(null);
            cliente.setNombreCompleto(null);
        }
    }

    private String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
