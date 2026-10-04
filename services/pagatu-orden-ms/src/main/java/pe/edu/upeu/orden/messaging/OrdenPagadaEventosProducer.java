package pe.edu.upeu.orden.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pe.edu.upeu.orden.event.OrdenPagadaEvento;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrdenPagadaEventosProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.ordenes-pagadas}")
    private String topic;

    public void publicarTrasCommit(OrdenPagadaEvento evento) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { enviar(evento); }
            });
        } else {
            enviar(evento);
        }
    }

    private void enviar(OrdenPagadaEvento evento) {
        kafkaTemplate.send(topic, String.valueOf(evento.getOrdenId()), evento)
                .whenComplete((resultado, ex) -> {
                    if (ex != null) {
                        log.error("component=producer topic={} eventType={} ordenId={} status=error error=\"{}\"",
                                topic, evento.getTipoEvento(), evento.getOrdenId(), ex.getMessage());
                        return;
                    }
                    log.info("component=producer topic={} partition={} offset={} eventType={} ordenId={} status=published",
                            resultado.getRecordMetadata().topic(), resultado.getRecordMetadata().partition(),
                            resultado.getRecordMetadata().offset(), evento.getTipoEvento(), evento.getOrdenId());
                });
    }
}
