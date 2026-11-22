package cl.sonidovivo.venta.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import cl.sonidovivo.venta.dto.*;
import cl.sonidovivo.venta.model.Venta;
import cl.sonidovivo.venta.service.VentaService;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Checkout, boletas e historial de ventas")
public class VentaController {

    private final VentaService ventaService;

    @Operation(summary = "Pagar y generar boleta",
            description = "Valida stock, simula el pago (tarjeta terminada en 0000 = rechazo, 402), crea el pedido y emite la boleta.")
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public Venta checkout(@RequestBody CheckoutRequest datos,
                          @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId) {
        // El dueño del pedido sale del token (lo entrega el Gateway), nunca del cuerpo de la petición
        return ventaService.checkout(datos, usuarioId);
    }

    @Operation(summary = "Listar ventas", description = "Filtro opcional por estado: APROBADO o RECHAZADO.")
    @GetMapping
    public List<Venta> listar(@RequestParam(required = false) String estado) {
        return ventaService.listar(estado);
    }

    @Operation(summary = "Resumen de ventas aprobadas", description = "Totales para el panel de administración.")
    @GetMapping("/resumen")
    public ResumenVentas resumen() {
        return ventaService.resumen();
    }

    @Operation(summary = "Buscar venta por número de boleta", description = "Ej: B-000001")
    @GetMapping("/boleta/{numero}")
    public Venta porBoleta(@PathVariable String numero) {
        return ventaService.obtenerPorBoleta(numero);
    }

    @Operation(summary = "Obtener venta por ID")
    @GetMapping("/{id}")
    public Venta obtener(@PathVariable Long id) {
        return ventaService.obtener(id);
    }
}
