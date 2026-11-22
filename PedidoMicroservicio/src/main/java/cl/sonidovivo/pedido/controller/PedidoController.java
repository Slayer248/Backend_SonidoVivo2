package cl.sonidovivo.pedido.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import cl.sonidovivo.pedido.dto.*;
import cl.sonidovivo.pedido.model.Pedido;
import cl.sonidovivo.pedido.service.PedidoService;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Creación y seguimiento de pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    @Operation(summary = "Listar pedidos", description = "Más recientes primero. Filtros opcionales: estado o email del cliente.")
    @GetMapping
    public List<Pedido> listar(@RequestParam(required = false) String estado,
                               @RequestParam(required = false) String email) {
        return pedidoService.listar(estado, email);
    }

    @Operation(summary = "Mis pedidos", description = "Pedidos de la cuenta con sesión iniciada (el Gateway entrega su id en X-Usuario-Id).")
    @GetMapping("/mios")
    public List<Pedido> mios(@RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId) {
        if (usuarioId == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión");
        }
        return pedidoService.listarDeUsuario(usuarioId);
    }

    @Operation(summary = "Obtener pedido por código", description = "Ej: PED-101. Un cliente solo puede ver los suyos.")
    @GetMapping("/{codigo}")
    public Pedido obtener(@PathVariable String codigo,
                          @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId,
                          @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        return pedidoService.obtenerParaUsuario(codigo, usuarioId, rol);
    }

    @Operation(summary = "Crear pedido", description = "Descuenta stock y calcula neto, IVA y total. 409 si no hay stock suficiente.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido crear(@RequestBody PedidoRequest datos) {
        return pedidoService.crear(datos);
    }

    @Operation(summary = "Cambiar estado del pedido", description = "Lo usan Administrador y Vendedor. Valores: En Preparación, Despachado, Entregado.")
    @PatchMapping("/{codigo}/estado")
    public Pedido cambiarEstado(@PathVariable String codigo, @RequestBody EstadoRequest datos) {
        return pedidoService.cambiarEstado(codigo, datos.estado());
    }
}
