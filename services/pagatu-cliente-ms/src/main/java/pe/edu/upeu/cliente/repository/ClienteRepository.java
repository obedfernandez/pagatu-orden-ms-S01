package pe.edu.upeu.cliente.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.cliente.entity.Cliente;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByKeycloakSub(String keycloakSub);
    boolean existsByDni(String dni);
    boolean existsByRuc(String ruc);
    boolean existsByDniAndIdNot(String dni, Long id);
    boolean existsByRucAndIdNot(String ruc, Long id);
}
