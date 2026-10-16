package pe.edu.upeu.cliente.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.cliente.entity.Cliente;
import pe.edu.upeu.cliente.mapper.ClienteMapper;
import pe.edu.upeu.cliente.repository.ClienteRepository;
import pe.edu.upeu.cliente.exception.ResourceNotFoundException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S07bClienteSubTest {
    @Mock ClienteRepository repository;
    @Mock ClienteMapper mapper;
    @InjectMocks ClienteServiceImpl service;

    @Test void noVinculaAutomaticamenteUsuarioACualquierPerfil() {
        String sub = "516ef047-ffbc-470f-b397-6d4a3fe8fbfa";
        when(repository.findByKeycloakSub(sub)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findByKeycloakSub(sub));
        verify(repository).findByKeycloakSub(sub);
    }

    @Test void buscaSoloPorSubVerificado() {
        String sub = "516ef047-ffbc-470f-b397-6d4a3fe8fbfa";
        Cliente cliente = Cliente.builder().id(1L).keycloakSub(sub).build();
        when(repository.findByKeycloakSub(sub)).thenReturn(Optional.of(cliente));
        service.findByKeycloakSub(sub);
        verify(mapper).toResponse(cliente);
    }
}
