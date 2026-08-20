package pe.edu.upeu.orden.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.orden.domain.*;
import pe.edu.upeu.orden.dto.*;
import pe.edu.upeu.orden.exception.ResourceNotFoundException;
import pe.edu.upeu.orden.mapper.OrdenMapper;
import pe.edu.upeu.orden.repository.OrdenRepository;
import java.util.List;

@Service @RequiredArgsConstructor
public class OrdenService {
    private final OrdenRepository repository; private final OrdenMapper mapper;
    @Transactional(readOnly=true) public List<OrdenResponse> listar(){ return repository.findAll().stream().map(mapper::toResponse).toList(); }
    @Transactional(readOnly=true) public OrdenResponse obtener(Long id){ return mapper.toResponse(buscarOFallar(id)); }
    @Transactional public OrdenResponse crear(OrdenRequest request){ return mapper.toResponse(repository.save(mapper.toEntity(request))); }
    @Transactional public OrdenResponse actualizar(Long id, OrdenRequest request){ Orden o=buscarOFallar(id); mapper.update(o,request); return mapper.toResponse(repository.save(o)); }
    @Transactional public void eliminar(Long id){ repository.delete(buscarOFallar(id)); }
    @Transactional(readOnly=true) public List<OrdenResponse> porCliente(Long clienteId){ return repository.findByClienteId(clienteId).stream().map(mapper::toResponse).toList(); }
    @Transactional(readOnly=true) public List<OrdenResponse> porEstado(EstadoOrden estado){ return repository.findByEstado(estado).stream().map(mapper::toResponse).toList(); }
    private Orden buscarOFallar(Long id){ return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + id)); }
}
