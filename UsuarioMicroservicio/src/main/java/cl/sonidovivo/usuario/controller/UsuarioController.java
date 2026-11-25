package cl.sonidovivo.usuario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import cl.sonidovivo.usuario.dto.*;
import cl.sonidovivo.usuario.service.UsuarioService;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Autenticación y gestión de usuarios por rol")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Iniciar sesión", description = "Valida correo y contraseña. 401 credenciales inválidas, 403 cuenta bloqueada.")
    @PostMapping("/login")
    public UsuarioResponse login(@RequestBody LoginRequest datos) {
        return usuarioService.login(datos);
    }

    @Operation(summary = "Renovar token",
            description = "Con un token vigente (encabezado Authorization: Bearer <token>) entrega los datos del usuario "
                    + "con un token nuevo que vence en 8 horas y lleva su rol actual. "
                    + "401 si el token venció o no es válido; 403 si la cuenta está bloqueada o debe cambiar su contraseña temporal.")
    @PostMapping("/renovar-token")
    public UsuarioResponse renovarToken(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return usuarioService.renovarToken(authorization);
    }

    @Operation(summary = "Registro público de cliente", description = "Crea un usuario con rol Cliente (formulario de registro del front).")
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@RequestBody RegistroUsuarioRequest datos) {
        return usuarioService.registrarCliente(datos);
    }

    @Operation(summary = "Listar usuarios", description = "Filtro opcional por texto (nombre, apellidos, correo, RUN) y por rol.")
    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(required = false) String buscar,
                                        @RequestParam(required = false) Long rolId) {
        return usuarioService.listar(buscar, rolId);
    }

    @Operation(summary = "Obtener usuario por ID")
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @Operation(summary = "Crear usuario (panel admin)", description = "Permite elegir el rol del nuevo usuario.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@RequestBody RegistroUsuarioRequest datos) {
        return usuarioService.crear(datos);
    }

    @Operation(summary = "Actualizar usuario")
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @RequestBody ActualizarUsuarioRequest datos,
                                      @RequestHeader(value = "X-Usuario-Rol", required = false) String rolSolicitante) {
        // El Gateway informa el rol de quien llama: solo un Administrador puede cambiar el rol desde aquí
        boolean puedeCambiarRol = rolSolicitante == null || "Administrador".equals(rolSolicitante);
        if (!puedeCambiarRol && datos.rolId() != null) {
            datos = new ActualizarUsuarioRequest(datos.run(), datos.nombre(), datos.apellidos(), datos.correo(),
                    null, datos.region(), datos.comuna(), datos.direccion());
        }
        return usuarioService.actualizar(id, datos);
    }

    @Operation(summary = "Cambiar rol de un usuario")
    @PatchMapping("/{id}/rol")
    public UsuarioResponse cambiarRol(@PathVariable Long id, @RequestBody CambioRolRequest datos) {
        return usuarioService.cambiarRol(id, datos.rolId());
    }

    @Operation(summary = "Bloquear o desbloquear usuario")
    @PatchMapping("/{id}/bloqueo")
    public UsuarioResponse cambiarBloqueo(@PathVariable Long id, @RequestBody BloqueoRequest datos,
                                          @RequestHeader(value = "X-Admin-Id", required = false) Long adminId) {
        return usuarioService.cambiarBloqueo(id, datos.bloqueado(), adminId);
    }

    @Operation(summary = "Cambiar contraseña", description = "Valida la contraseña actual.")
    @PutMapping("/{id}/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(@PathVariable Long id, @RequestBody CambiarContrasenaRequest datos) {
        usuarioService.cambiarContrasena(id, datos);
    }

    @Operation(summary = "Inhabilitar usuario", description = "Bloquea la cuenta sin borrar el registro.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @RequestHeader(value = "X-Admin-Id", required = false) Long adminId) {
        usuarioService.eliminar(id, adminId);
    }
}
