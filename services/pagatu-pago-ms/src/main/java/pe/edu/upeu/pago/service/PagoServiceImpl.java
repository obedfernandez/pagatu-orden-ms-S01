package pe.edu.upeu.pago.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pago.dto.PagoResponse;
import pe.edu.upeu.pago.entity.EstadoPago;
import pe.edu.upeu.pago.entity.Pago;
import pe.edu.upeu.pago.event.OrdenCreadaEvento;
import pe.edu.upeu.pago.event.PagoValidadoEvento;
import pe.edu.upeu.pago.exception.ResourceNotFoundException;
import pe.edu.upeu.pago.mapper.PagoMapper;
import pe.edu.upeu.pago.repository.PagoRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {
    private static final String PAGO_VALIDADO = "pago.validado";
    private static final String PAGO_FALLIDO = "pago.fallido";
    private static final String METODO_RECHAZADO = "TARJETA_RECHAZADA";
    private final PagoRepository pagoRepository;
    private final PagoPersistenciaService persistenciaService;
    private final PagoMapper pagoMapper;

    @Value("${spring.application.name}")
    private String nombreServicio;

    @Override
    public void procesar(OrdenCreadaEvento orden) {
        if (orden == null || orden.getOrdenId() == null || orden.getTotal() == null) {
            log.warn("component=processor status=ignored motivo=evento_invalido");
            return;
        }
        // Guard 1: an ordinary Kafka redelivery must have no second business effect.
        if (pagoRepository.findByOrdenId(orden.getOrdenId()).isPresent()) {
            log.warn("component=processor ordenId={} status=ignored motivo=\"pago ya procesado (evento duplicado)\"",
                    orden.getOrdenId());
            return;
        }
        boolean rechazado = METODO_RECHAZADO.equalsIgnoreCase(orden.getMetodoPago());
        EstadoPago estado = rechazado ? EstadoPago.FALLIDO : EstadoPago.VALIDADO;
        String tipo = rechazado ? PAGO_FALLIDO : PAGO_VALIDADO;

        try {
            // Guard 2: the DB UNIQUE(orden_id) wins if consumers raced. A separate
            // transaction boundary allows catching the constraint failure after rollback.
            Pago pago = persistenciaService.guardarYPublicar(orden, estado, tipo, nombreServicio);
            log.info("component=processor ordenId={} estado={} status=processed", pago.getOrdenId(), pago.getEstado());
        } catch (DataIntegrityViolationException ex) {
            if (pagoRepository.findByOrdenId(orden.getOrdenId()).isPresent()) {
                log.warn("component=processor ordenId={} status=ignored motivo=\"pago ya procesado (condicion de carrera)\"",
                        orden.getOrdenId());
                return;
            }
            // A different integrity error is not a duplicate. Kafka must retry it.
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listar() {
        return pagoRepository.findAll().stream().map(pagoMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse obtener(Long id) {
        return pagoMapper.toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PagoResponse> buscarPorOrden(Long ordenId) {
        return pagoRepository.findByOrdenId(ordenId).map(pagoMapper::toResponse);
    }

    private Pago buscarOFallar(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + id));
    }
}
