package cl.sonidovivo.usuario.service;

import cl.sonidovivo.usuario.client.RolClient;
import cl.sonidovivo.usuario.dto.*;
import cl.sonidovivo.usuario.model.Usuario;
import cl.sonidovivo.usuario.repository.UsuarioRepository;
import cl.sonidovivo.usuario.util.JwtUtil;
import cl.sonidovivo.usuario.util.GeneradorContrasena;
import cl.sonidovivo.usuario.util.RutUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    /** Cuenta administradora principal: no se puede bloquear ni quitarle el rol. */
    public static final String ADMIN_PRINCIPAL = "admin@sonidovivo.cl";
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int LARGO_MIN_CONTRASENA = 8;
    private static final int LARGO_CONTRASENA_TEMPORAL = 10;

    private final UsuarioRepository usuarioRepository;
    private final RolClient rolClient;
    private final CorreoService correoService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiracion-segundos:28800}")
    private long jwtExpiracion;

    public UsuarioResponse login(LoginRequest d) {
        if (d.correo() == null || d.correo().isBlank() || d.contrasena() == null || d.contrasena().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo y contraseña son obligatorios");
        }
        Usuario u = usuarioRepository.findByCorreoIgnoreCase(d.correo().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));
        if (!passwordEncoder.matches(d.contrasena(), u.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }
        if (Boolean.TRUE.equals(u.getBloqueado())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tu cuenta se encuentra bloqueada. Contacta al administrador.");
        }
        UsuarioResponse r = respuesta(u);
        return r.conToken(JwtUtil.crear(u.getId(), r.rol(), u.getCorreo(), jwtExpiracion, jwtSecret,
                Boolean.TRUE.equals(u.getDebeCambiarContrasena())));
    }

    /**
     * Renovación del token: con un token todavía vigente entrega uno nuevo, con el vencimiento renovado.
     * El usuario se vuelve a leer desde la base de datos, así el nuevo token lleva su rol actual
     * y una cuenta bloqueada (o con contraseña temporal) no puede seguir renovando su sesión.
     */
    public UsuarioResponse renovarToken(String authorization) {
        String token = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7).trim() : null;
        JwtUtil.DatosToken datos = JwtUtil.verificar(token, jwtSecret);
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tu sesión expiró o no es válida. Inicia sesión nuevamente");
        }
        Usuario u = usuarioRepository.findById(datos.usuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tu sesión expiró o no es válida. Inicia sesión nuevamente"));
        if (Boolean.TRUE.equals(u.getBloqueado())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tu cuenta se encuentra bloqueada. Contacta al administrador.");
        }
        if (Boolean.TRUE.equals(u.getDebeCambiarContrasena())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Debes cambiar tu contraseña temporal antes de continuar");
        }
        UsuarioResponse r = respuesta(u);
        return r.conToken(JwtUtil.crear(u.getId(), r.rol(), u.getCorreo(), jwtExpiracion, jwtSecret));
    }

    public List<UsuarioResponse> listar(String buscar, Long rolId) {
        Map<Long, String> roles = rolClient.obtenerMapaRoles();
        String filtro = buscar == null ? "" : buscar.trim().toLowerCase();
        return usuarioRepository.findAllByOrderByIdAsc().stream()
                .filter(u -> rolId == null || u.getRolId().equals(rolId))
                .filter(u -> filtro.isEmpty()
                        || u.getNombre().toLowerCase().contains(filtro)
                        || (u.getApellidos() != null && u.getApellidos().toLowerCase().contains(filtro))
                        || u.getCorreo().toLowerCase().contains(filtro)
                        || u.getRun().toLowerCase().contains(filtro))
                .map(u -> UsuarioResponse.desde(u, roles.getOrDefault(u.getRolId(), "Desconocido")))
                .toList();
    }

    public UsuarioResponse obtener(Long id) {
        return respuesta(buscar(id));
    }

    /** Registro público (formulario de registro del front): siempre rol Cliente. La contraseña se genera y se envía por correo. */
    @Transactional
    public UsuarioResponse registrarCliente(RegistroUsuarioRequest d) {
        return guardarNuevo(d, rolClient.obtenerPorNombre("Cliente").id());
    }

    /** Alta desde el panel de administración, con rol elegido. La contraseña se genera y se envía por correo. */
    @Transactional
    public UsuarioResponse crear(RegistroUsuarioRequest d) {
        if (d.rolId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol es obligatorio");
        }
        return guardarNuevo(d, rolClient.obtenerPorId(d.rolId()).id());
    }

    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest d) {
        Usuario u = buscar(id);
        validarCamposBase(d.run(), d.nombre(), d.correo());
        String run = RutUtil.normalizar(d.run());
        String correo = d.correo().trim();
        if (usuarioRepository.existsByRunAndIdNot(run, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El RUN '" + run + "' ya está registrado");
        }
        if (usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(correo, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo '" + correo + "' ya está en uso");
        }
        if (ADMIN_PRINCIPAL.equalsIgnoreCase(u.getCorreo()) && !ADMIN_PRINCIPAL.equalsIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede cambiar el correo del administrador principal");
        }
        Long rolId = d.rolId() != null ? d.rolId() : u.getRolId();
        validarRolAdminPrincipal(u, rolId);
        u.setRun(run);
        u.setNombre(d.nombre().trim());
        u.setApellidos(d.apellidos() == null ? null : d.apellidos().trim());
        u.setCorreo(correo);
        u.setRolId(rolClient.obtenerPorId(rolId).id());
        u.setRegion(d.region());
        u.setComuna(d.comuna());
        u.setDireccion(d.direccion());
        return respuesta(usuarioRepository.save(u));
    }

    public UsuarioResponse cambiarRol(Long id, Long rolId) {
        Usuario u = buscar(id);
        if (rolId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol es obligatorio");
        }
        validarRolAdminPrincipal(u, rolId);
        u.setRolId(rolClient.obtenerPorId(rolId).id());
        return respuesta(usuarioRepository.save(u));
    }

    public UsuarioResponse cambiarBloqueo(Long id, Boolean bloqueado, Long adminId) {
        Usuario u = buscar(id);
        if (bloqueado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el estado de bloqueo");
        }
        if (bloqueado) {
            if (adminId != null && adminId.equals(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes bloquear tu propia cuenta");
            }
            if (ADMIN_PRINCIPAL.equalsIgnoreCase(u.getCorreo())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede bloquear al administrador principal");
            }
        }
        u.setBloqueado(bloqueado);
        return respuesta(usuarioRepository.save(u));
    }

    public void cambiarContrasena(Long id, CambiarContrasenaRequest d) {
        Usuario u = buscar(id);
        if (d.contrasenaActual() == null || !passwordEncoder.matches(d.contrasenaActual(), u.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual no coincide");
        }
        validarContrasena(d.contrasenaNueva());
        if (d.contrasenaNueva().equals(d.contrasenaActual())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña nueva debe ser distinta de la actual");
        }
        u.setContrasena(passwordEncoder.encode(d.contrasenaNueva()));
        u.setDebeCambiarContrasena(false);
        usuarioRepository.save(u);
    }

    /** Inhabilita la cuenta (no borra el registro, para conservar historial de compras). */
    public void eliminar(Long id, Long adminId) {
        cambiarBloqueo(id, true, adminId);
    }

    // ---------- internos ----------

    private UsuarioResponse guardarNuevo(RegistroUsuarioRequest d, Long rolId) {
        validarCamposBase(d.run(), d.nombre(), d.correo());
        String run = RutUtil.normalizar(d.run());
        String correo = d.correo().trim();
        if (usuarioRepository.existsByRun(run)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El RUN '" + run + "' ya está registrado");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo '" + correo + "' ya está registrado");
        }
        // La contraseña la define el sistema (se ignora la que venga en la petición) y se envía al correo.
        // Si el correo falla se lanza 503 y, al ser transaccional, la cuenta no queda creada.
        String temporal = GeneradorContrasena.generar(LARGO_CONTRASENA_TEMPORAL);
        Usuario nuevo = Usuario.builder()
                .run(run)
                .nombre(d.nombre().trim())
                .apellidos(d.apellidos() == null ? null : d.apellidos().trim())
                .correo(correo)
                .contrasena(passwordEncoder.encode(temporal))
                .rolId(rolId)
                .region(d.region()).comuna(d.comuna()).direccion(d.direccion())
                .bloqueado(false)
                .debeCambiarContrasena(true)
                .build();
        UsuarioResponse creado = respuesta(usuarioRepository.save(nuevo));
        correoService.enviarContrasenaTemporal(nuevo.getNombre(), correo, temporal);
        return creado;
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private UsuarioResponse respuesta(Usuario u) {
        return UsuarioResponse.desde(u, rolClient.obtenerPorId(u.getRolId()).nombre());
    }

    private void validarRolAdminPrincipal(Usuario u, Long nuevoRolId) {
        if (ADMIN_PRINCIPAL.equalsIgnoreCase(u.getCorreo()) && !nuevoRolId.equals(u.getRolId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede cambiar el rol del administrador principal");
        }
    }

    private void validarCamposBase(String run, String nombre, String correo) {
        if (run == null || run.isBlank() || !RutUtil.esValido(run)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El RUN no es válido");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        if (correo == null || !EMAIL.matcher(correo.trim()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo electrónico no tiene un formato válido");
        }
    }

    private void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < LARGO_MIN_CONTRASENA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos " + LARGO_MIN_CONTRASENA + " caracteres");
        }
    }
}
