package pe.edu.upeu.cliente.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.cliente.entity.NotificacionPago;
import pe.edu.upeu.cliente.event.OrdenPagadaEvento;
import pe.edu.upeu.cliente.repository.NotificacionPagoRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrdenPagadaEventosConsumer {
    private static final String ORDEN_PAGADA = "orden.pagada";
    private final NotificacionPagoRepository repository;

    @KafkaListener(topics = "${app.kafka.topic.ordenes-pagadas}")
    @Transactional
    public void alRecibirOrdenPagada(OrdenPagadaEvento evento) {
        if (evento == null) {
            log.warn("component=consumer eventType=desconocido status=invalid");
            return;
        }
        if (!ORDEN_PAGADA.equals(evento.getTipoEvento())) {
            log.warn("component=consumer eventType={} status=ignored", evento.getTipoEvento());
            return;
        }

        log.info("component=consumer eventType={} ordenId={} idCliente={} status=consumed",
                evento.getTipoEvento(), evento.getOrdenId(), evento.getIdCliente());

        if (repository.existsByOrdenId(evento.getOrdenId())) {
            log.info("component=notification ordenId={} status=ignored motivo=duplicado", evento.getOrdenId());
            return;
        }

        repository.save(NotificacionPago.builder()
                .ordenId(evento.getOrdenId())
                .idCliente(evento.getIdCliente())
                .monto(evento.getMonto())
                .canal(evento.getCanalNotificacion())
                .estado("COMPROBANTE_REGISTRADO")
                .fechaRegistro(LocalDateTime.now())
                .build());

        log.info("component=notification ordenId={} idCliente={} canal={} status=processed",
                evento.getOrdenId(), evento.getIdCliente(), evento.getCanalNotificacion());
    }
}
