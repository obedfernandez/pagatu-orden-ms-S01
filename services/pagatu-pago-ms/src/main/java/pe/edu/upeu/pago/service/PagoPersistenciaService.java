package pe.edu.upeu.pago.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pago.entity.EstadoPago;
import pe.edu.upeu.pago.entity.Pago;
import pe.edu.upeu.pago.event.OrdenCreadaEvento;
import pe.edu.upeu.pago.event.PagoValidadoEvento;
import pe.edu.upeu.pago.messaging.PagoEventosProducer;
import pe.edu.upeu.pago.repository.PagoRepository;

import java.time.Instant;
import java.time.LocalDateTime;

/** One local DB transaction; UNIQUE(orden_id) remains the concurrency backstop. */
@Service
@RequiredArgsConstructor
public class PagoPersistenciaService {
    private final PagoRepository pagoRepository;
    private final PagoEventosProducer producer;

    @Transactional
    public Pago guardarYPublicar(OrdenCreadaEvento orden, EstadoPago estado, String tipoEvento, String origen) {
        // flush forces UNIQUE errors inside this method, before registering the event.
        Pago pago = pagoRepository.saveAndFlush(Pago.builder()
                .ordenId(orden.getOrdenId())
                .monto(orden.getTotal())
                .metodoPago(orden.getMetodoPago())
                .estado(estado)
                .fechaPago(LocalDateTime.now())
                .build());

        producer.publicarTrasCommit(PagoValidadoEvento.builder()
                .tipoEvento(tipoEvento)
                .ordenId(pago.getOrdenId())
                .monto(pago.getMonto())
                .estado(pago.getEstado().name())
                .origen(origen)
                .timestamp(Instant.now().toEpochMilli())
                .build());
        return pago;
    }
}
