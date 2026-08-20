package pe.edu.upeu.orden.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.orden.domain.Orden;
import pe.edu.upeu.orden.domain.EstadoOrden;
import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findByClienteId(Long clienteId);
    List<Orden> findByEstado(EstadoOrden estado);
}
