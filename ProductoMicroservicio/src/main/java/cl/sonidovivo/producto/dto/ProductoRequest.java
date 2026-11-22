package cl.sonidovivo.producto.dto;

public record ProductoRequest(String nombre, String categoria, Integer precio, Integer stock, String img) {}
