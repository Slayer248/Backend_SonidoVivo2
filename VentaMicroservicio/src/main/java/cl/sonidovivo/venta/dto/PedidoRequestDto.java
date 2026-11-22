package cl.sonidovivo.venta.dto;

import java.util.List;

public record PedidoRequestDto(String clienteNombre, String rut, String email, String direccion,
                               Long puntoRetiroId, List<ItemRequest> items, Long usuarioId) {}
