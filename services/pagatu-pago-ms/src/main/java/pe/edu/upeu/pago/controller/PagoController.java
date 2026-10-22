package pe.edu.upeu.pago.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import pe.edu.upeu.pago.dto.PagoResponse;
import pe.edu.upeu.pago.service.PagoService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagoController {
    private final PagoService pagoService;

    @GetMapping
    public List<PagoResponse> listar() { return pagoService.listar(); }

    @GetMapping("/por-orden/{ordenId}")
    public ResponseEntity<PagoResponse> obtenerPorOrden(@PathVariable Long ordenId) {
        return pagoService.buscarPorOrden(ordenId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public PagoResponse obtener(@PathVariable Long id) { return pagoService.obtener(id); }
}
