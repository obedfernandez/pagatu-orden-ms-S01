package pe.edu.upeu.orden.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.orden.entity.EstadoOrden;
import pe.edu.upeu.orden.entity.Orden;
import pe.edu.upeu.orden.entity.OrdenDetalle;
import pe.edu.upeu.orden.messaging.OrdenEventosProducer;
import pe.edu.upeu.orden.messaging.OrdenPagadaEventosProducer;
import pe.edu.upeu.orden.repository.OrdenRepository;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S09CompensacionTest {
    @Mock OrdenRepository repository;
    @Mock ProductoConsultaService catalogo;
    @Mock OrdenEventosProducer ordenProducer;
    @Mock OrdenPagadaEventosProducer pagadaProducer;
    @InjectMocks OrdenServiceImpl service;

    @Test void pagoFallidoCancelaYDevuelveStock() {
        Orden orden = Orden.builder().id(40L).estado(EstadoOrden.PENDIENTE_PAGO)
                .detalles(List.of(OrdenDetalle.builder().idProducto(1L).nombreProducto("Producto")
                        .cantidad(2).build())).build();
        when(repository.findByIdParaTransicion(40L)).thenReturn(Optional.of(orden));
        when(catalogo.restaurarStock(1L, 2)).thenReturn(true);
        service.compensar(40L);
        assertEquals(EstadoOrden.CANCELADA, orden.getEstado());
        verify(catalogo).restaurarStock(1L, 2);
        verify(repository).save(orden);
        verifyNoInteractions(pagadaProducer);
    }

    @Test void compensacionDuplicadaNoDevuelveStockNuevamente() {
        Orden cancelada = Orden.builder().id(41L).estado(EstadoOrden.CANCELADA).build();
        when(repository.findByIdParaTransicion(41L)).thenReturn(Optional.of(cancelada));
        service.compensar(41L);
        assertEquals(EstadoOrden.CANCELADA, cancelada.getEstado());
        verifyNoInteractions(catalogo);
        verify(repository, never()).save(any());
    }

    @Test void pagadaNoPuedeCompensarse() {
        Orden pagada = Orden.builder().id(42L).estado(EstadoOrden.PAGADA).build();
        when(repository.findByIdParaTransicion(42L)).thenReturn(Optional.of(pagada));
        service.compensar(42L);
        assertEquals(EstadoOrden.PAGADA, pagada.getEstado());
        verifyNoInteractions(catalogo);
    }
}
