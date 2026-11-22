package cl.sonidovivo.producto.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import cl.sonidovivo.producto.dto.*;
import cl.sonidovivo.producto.model.Producto;
import cl.sonidovivo.producto.service.ProductoService;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Catálogo de productos e inventario")
public class ProductoController {

    private final ProductoService productoService;

    @Operation(summary = "Listar productos", description = "Permite filtrar por texto en el nombre y por categoría.")
    @GetMapping
    public List<Producto> listar(@RequestParam(required = false) String buscar,
                                 @RequestParam(required = false) String categoria) {
        return productoService.listar(buscar, categoria);
    }

    @Operation(summary = "Listar categorías disponibles")
    @GetMapping("/categorias")
    public List<String> categorias() {
        return productoService.categorias();
    }

    @Operation(summary = "Obtener producto por ID")
    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    @Operation(summary = "Crear producto")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crear(@RequestBody ProductoRequest datos) {
        return productoService.crear(datos);
    }

    @Operation(summary = "Actualizar producto")
    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody ProductoRequest datos) {
        return productoService.actualizar(id, datos);
    }

    @Operation(summary = "Eliminar producto")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }

    @Operation(summary = "Descontar stock", description = "Uso interno de Pedidos. Retorna 409 si no hay stock suficiente.")
    @PatchMapping("/{id}/stock/descuento")
    public Producto descontarStock(@PathVariable Long id, @RequestBody StockRequest datos) {
        return productoService.descontarStock(id, datos.cantidad());
    }

    @Operation(summary = "Reponer stock", description = "Uso interno de Pedidos para revertir un descuento.")
    @PatchMapping("/{id}/stock/reposicion")
    public Producto reponerStock(@PathVariable Long id, @RequestBody StockRequest datos) {
        return productoService.reponerStock(id, datos.cantidad());
    }
}
