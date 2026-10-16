package pe.edu.upeu.orden.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.orden.event.PagoValidadoEvento;
import pe.edu.upeu.orden.service.OrdenService;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S09PagoEventosConsumerTest {
    @Mock OrdenService ordenService;
    @InjectMocks PagoEventosConsumer consumer;

    @Test void enrutaPagoFallido() {
        consumer.alRecibirPago(PagoValidadoEvento.builder().tipoEvento("pago.fallido").ordenId(1L).build());
        verify(ordenService).compensar(1L);
        verify(ordenService, never()).marcarPagada(any());
    }

    @Test void enrutaPagoValidado() {
        consumer.alRecibirPago(PagoValidadoEvento.builder().tipoEvento("pago.validado").ordenId(2L).build());
        verify(ordenService).marcarPagada(2L);
        verify(ordenService, never()).compensar(any());
    }

    @Test void ignoraEventoDesconocido() {
        consumer.alRecibirPago(PagoValidadoEvento.builder().tipoEvento("foo").ordenId(3L).build());
        verifyNoInteractions(ordenService);
    }
}
