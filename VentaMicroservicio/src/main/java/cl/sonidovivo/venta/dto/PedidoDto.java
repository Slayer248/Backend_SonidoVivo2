package cl.sonidovivo.venta.dto;

public record PedidoDto(String codigo, Integer neto, Integer iva, Integer total, String estado) {}
