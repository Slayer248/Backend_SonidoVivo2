package cl.sonidovivo.usuario.dto;

public record ActualizarUsuarioRequest(String run, String nombre, String apellidos, String correo,
                                       Long rolId, String region, String comuna, String direccion) {}
