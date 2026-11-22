package cl.sonidovivo.usuario.dto;

/** rolId solo lo usa el panel de administración; en el registro público se ignora (siempre Cliente). */
public record RegistroUsuarioRequest(String run, String nombre, String apellidos, String correo, String contrasena,
                                     Long rolId, String region, String comuna, String direccion) {}
