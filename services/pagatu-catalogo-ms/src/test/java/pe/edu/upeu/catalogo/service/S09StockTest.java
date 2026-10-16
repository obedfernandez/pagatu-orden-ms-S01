package pe.edu.upeu.catalogo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upeu.catalogo.entity.Producto;
import pe.edu.upeu.catalogo.exception.StockInsuficienteException;
import pe.edu.upeu.catalogo.mapper.ProductoMapper;
import pe.edu.upeu.catalogo.repository.CategoriaRepository;
import pe.edu.upeu.catalogo.repository.ProductoRepository;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S09StockTest {
    @Mock ProductoRepository repository;
    @Mock ProductoMapper mapper;
    @Mock CategoriaRepository categoriaRepository;
    @InjectMocks ProductoService service;

    @Test void descuentaYRestituye() {
        Producto producto = Producto.builder().id(1L).stock(10).build();
        when(repository.findByIdParaStock(1L)).thenReturn(Optional.of(producto));
        service.descontarStock(1L, 2);
        assertEquals(8, producto.getStock());
        service.restaurarStock(1L, 2);
        assertEquals(10, producto.getStock());
    }

    @Test void faltaStockEs409() {
        when(repository.findByIdParaStock(1L)).thenReturn(Optional.of(Producto.builder().stock(1).build()));
        assertThrows(StockInsuficienteException.class, () -> service.descontarStock(1L, 2));
    }

    @Test void cantidadCeroSeRechaza() {
        assertThrows(ResponseStatusException.class, () -> service.descontarStock(1L, 0));
        verifyNoInteractions(repository);
    }
}
