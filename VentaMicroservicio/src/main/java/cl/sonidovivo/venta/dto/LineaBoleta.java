package cl.sonidovivo.venta.dto;

/** Una línea de la boleta que se envía por correo. */
public record LineaBoleta(String nombre, int cantidad, int precioUnitario) {}
