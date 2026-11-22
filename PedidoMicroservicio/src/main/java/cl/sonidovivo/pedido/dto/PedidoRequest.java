package cl.sonidovivo.pedido.dto;

import java.util.List;

/** Solo se envían ids y cantidades: los precios los toma el servidor desde Productos. */
public record PedidoRequest(String clienteNombre, String rut, String email, String direccion,
                            Long puntoRetiroId, List<ItemRequest> items, Long usuarioId) {}
