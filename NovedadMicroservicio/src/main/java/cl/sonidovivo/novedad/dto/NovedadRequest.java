package cl.sonidovivo.novedad.dto;

import java.time.LocalDate;

public record NovedadRequest(String titulo, String categoria, LocalDate fecha, String autor,
                             String imagen, String resumen, String descripcionCompleta) {}
