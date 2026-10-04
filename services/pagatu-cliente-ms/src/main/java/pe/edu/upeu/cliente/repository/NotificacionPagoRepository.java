package pe.edu.upeu.cliente.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.cliente.entity.NotificacionPago;

public interface NotificacionPagoRepository extends JpaRepository<NotificacionPago, Long> {
    boolean existsByOrdenId(Long ordenId);
}
