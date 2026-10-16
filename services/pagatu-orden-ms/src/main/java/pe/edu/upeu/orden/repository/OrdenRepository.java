package pe.edu.upeu.orden.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.orden.entity.Orden;

import java.util.Optional;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Orden o WHERE o.id = :id")
    Optional<Orden> findByIdParaTransicion(@Param("id") Long id);
}
