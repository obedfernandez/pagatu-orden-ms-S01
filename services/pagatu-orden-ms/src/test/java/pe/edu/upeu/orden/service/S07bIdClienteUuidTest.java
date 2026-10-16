package pe.edu.upeu.orden.service;

import org.junit.jupiter.api.Test;
import pe.edu.upeu.orden.entity.Orden;
import pe.edu.upeu.orden.event.OrdenCreadaEvento;
import static org.junit.jupiter.api.Assertions.assertEquals;

class S07bIdClienteUuidTest {
    @Test void uuidSeConservaEnLaOrdenYEnElEventoKafka() {
        String sub = "516ef047-ffbc-470f-b397-6d4a3fe8fbfa";
        Orden orden = Orden.builder().idCliente(sub).build();
        OrdenCreadaEvento evento = OrdenCreadaEvento.builder().idCliente(orden.getIdCliente()).build();
        assertEquals(sub, evento.getIdCliente());
    }
}
