package cl.sonidovivo.puntoretiro.dto;

public record PuntoRetiroRequest(String nombre, String tipo, String direccion, String horario,
                                 Double lat, Double lng, Boolean principal) {}
