package pe.edu.upeu.orden.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.orden.domain.Orden;
import pe.edu.upeu.orden.dto.*;
import java.math.BigDecimal;

@Component
public class OrdenMapper {
    public Orden toEntity(OrdenRequest r) {
        var total = r.precioUnitario().multiply(BigDecimal.valueOf(r.cantidad()));
        return Orden.builder().clienteId(r.clienteId()).conceptoId(r.conceptoId()).cantidad(r.cantidad())
                .precioUnitario(r.precioUnitario()).total(total).estado(r.estado()).build();
    }
    public void update(Orden o, OrdenRequest r) {
        o.setClienteId(r.clienteId()); o.setConceptoId(r.conceptoId()); o.setCantidad(r.cantidad());
        o.setPrecioUnitario(r.precioUnitario()); o.setTotal(r.precioUnitario().multiply(BigDecimal.valueOf(r.cantidad())));
        if (r.estado() != null) o.setEstado(r.estado());
    }
    public OrdenResponse toResponse(Orden o) {
        return new OrdenResponse(o.getId(), o.getClienteId(), o.getConceptoId(), o.getCantidad(), o.getPrecioUnitario(), o.getTotal(), o.getEstado(), o.getFechaCreacion());
    }
}
