package cl.sonidovivo.usuario.dto;

import cl.sonidovivo.usuario.model.Usuario;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Nunca incluye la contraseña. El campo "rol" lleva el nombre del rol (Administrador, Vendedor, Cliente).
 * "token" solo viaja en la respuesta del login; en el resto de las respuestas es null y no se serializa.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UsuarioResponse(Long id, String run, String nombre, String apellidos, String correo,
                              Long rolId, String rol, String region, String comuna, String direccion,
                              Boolean bloqueado, Boolean debeCambiarContrasena, String token) {
    public static UsuarioResponse desde(Usuario u, String rolNombre) {
        return new UsuarioResponse(u.getId(), u.getRun(), u.getNombre(), u.getApellidos(), u.getCorreo(),
                u.getRolId(), rolNombre, u.getRegion(), u.getComuna(), u.getDireccion(), u.getBloqueado(),
                u.getDebeCambiarContrasena(), null);
    }

    public UsuarioResponse conToken(String nuevoToken) {
        return new UsuarioResponse(id, run, nombre, apellidos, correo, rolId, rol, region, comuna, direccion,
                bloqueado, debeCambiarContrasena, nuevoToken);
    }
}
