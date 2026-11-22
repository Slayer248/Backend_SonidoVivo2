package cl.sonidovivo.pedido.dto;

public record ProductoDto(Long id, String nombre, String categoria, Integer precio, Integer stock, String img) {}
