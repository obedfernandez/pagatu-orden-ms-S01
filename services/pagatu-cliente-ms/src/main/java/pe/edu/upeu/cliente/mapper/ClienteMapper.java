package pe.edu.upeu.cliente.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import pe.edu.upeu.cliente.dto.ClienteRequest;
import pe.edu.upeu.cliente.dto.ClienteResponse;
import pe.edu.upeu.cliente.entity.Cliente;

@Mapper(componentModel = "spring")
public interface ClienteMapper {
    Cliente toEntity(ClienteRequest request);
    ClienteResponse toResponse(Cliente cliente);
    void updateEntity(ClienteRequest request, @MappingTarget Cliente cliente);
}
