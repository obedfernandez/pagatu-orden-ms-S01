package pe.edu.upeu.pago.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import pe.edu.upeu.pago.entity.EstadoPago;
import pe.edu.upeu.pago.entity.Pago;
import pe.edu.upeu.pago.event.OrdenCreadaEvento;
import pe.edu.upeu.pago.mapper.PagoMapper;
import pe.edu.upeu.pago.repository.PagoRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S09PagoServiceTest {
    @Mock PagoRepository repository;
    @Mock PagoPersistenciaService persistencia;
    @Mock PagoMapper mapper;
    @InjectMocks PagoServiceImpl service;

    private OrdenCreadaEvento evento(Long id, String metodo) {
        return OrdenCreadaEvento.builder().ordenId(id).total(new BigDecimal("89.90"))
                .metodoPago(metodo).tipoEvento("orden.creada").build();
    }

    @Test void duplicadoSeIgnoraSinCrearOtroPago() {
        when(repository.findByOrdenId(17L)).thenReturn(Optional.of(Pago.builder().ordenId(17L).build()));
        service.procesar(evento(17L, "YAPE_PLIN"));
        verifyNoInteractions(persistencia);
    }

    @Test void pagoRechazadoPublicaEventoFallido() {
        when(repository.findByOrdenId(20L)).thenReturn(Optional.empty());
        when(persistencia.guardarYPublicar(any(), eq(EstadoPago.FALLIDO), eq("pago.fallido"), any()))
                .thenReturn(Pago.builder().ordenId(20L).estado(EstadoPago.FALLIDO).build());
        service.procesar(evento(20L, "TARJETA_RECHAZADA"));
        verify(persistencia).guardarYPublicar(any(), eq(EstadoPago.FALLIDO), eq("pago.fallido"), any());
    }

    @Test void pagoAprobadoMantieneEventoDeS08() {
        when(repository.findByOrdenId(21L)).thenReturn(Optional.empty());
        when(persistencia.guardarYPublicar(any(), eq(EstadoPago.VALIDADO), eq("pago.validado"), any()))
                .thenReturn(Pago.builder().ordenId(21L).estado(EstadoPago.VALIDADO).build());
        service.procesar(evento(21L, "YAPE_PLIN"));
        verify(persistencia).guardarYPublicar(any(), eq(EstadoPago.VALIDADO), eq("pago.validado"), any());
    }

    @Test void carreraUniqueSeIgnoraSoloSiExisteElPago() {
        when(repository.findByOrdenId(22L))
                .thenReturn(Optional.empty(), Optional.of(Pago.builder().ordenId(22L).build()));
        when(persistencia.guardarYPublicar(any(), any(), any(), any()))
                .thenThrow(new DataIntegrityViolationException("uk_pagos_orden"));
        assertDoesNotThrow(() -> service.procesar(evento(22L, "YAPE_PLIN")));
    }

    @Test void errorDistintoAUnDuplicadoSePropaga() {
        when(repository.findByOrdenId(23L)).thenReturn(Optional.empty());
        when(persistencia.guardarYPublicar(any(), any(), any(), any()))
                .thenThrow(new DataIntegrityViolationException("otro constraint"));
        assertThrows(DataIntegrityViolationException.class,
                () -> service.procesar(evento(23L, "YAPE_PLIN")));
    }
}
