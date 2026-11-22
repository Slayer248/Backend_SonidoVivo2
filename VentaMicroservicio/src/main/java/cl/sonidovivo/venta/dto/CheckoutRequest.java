package cl.sonidovivo.venta.dto;

import java.util.List;

public record CheckoutRequest(String clienteNombre, String rut, String email, String direccion,
                              Long puntoRetiroId, String numeroTarjeta, List<ItemRequest> items) {}
