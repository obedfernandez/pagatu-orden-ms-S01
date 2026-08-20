package pe.edu.upeu.orden.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.orden.domain.EstadoOrden;
import pe.edu.upeu.orden.dto.*;
import pe.edu.upeu.orden.service.OrdenService;
import java.util.List;

@RestController @RequestMapping("/api/v1/ordenes") @RequiredArgsConstructor @Tag(name="Ordenes")
public class OrdenController {
    private final OrdenService service;
    @GetMapping @Operation(summary="Listar ordenes") public List<OrdenResponse> listar(){ return service.listar(); }
    @GetMapping("/{id}") @Operation(summary="Obtener orden") public OrdenResponse obtener(@PathVariable Long id){ return service.obtener(id); }
    @GetMapping(params="clienteId") public List<OrdenResponse> porCliente(@RequestParam Long clienteId){ return service.porCliente(clienteId); }
    @GetMapping(params="estado") public List<OrdenResponse> porEstado(@RequestParam EstadoOrden estado){ return service.porEstado(estado); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @Operation(summary="Crear orden") public OrdenResponse crear(@Valid @RequestBody OrdenRequest r){ return service.crear(r); }
    @PutMapping("/{id}") @Operation(summary="Actualizar orden") public OrdenResponse actualizar(@PathVariable Long id,@Valid @RequestBody OrdenRequest r){ return service.actualizar(id,r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @Operation(summary="Eliminar orden") public void eliminar(@PathVariable Long id){ service.eliminar(id); }
}
