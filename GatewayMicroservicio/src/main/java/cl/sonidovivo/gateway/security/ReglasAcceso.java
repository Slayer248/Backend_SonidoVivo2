package cl.sonidovivo.gateway.security;

import org.springframework.http.HttpMethod;

import java.util.Set;
import java.util.regex.Pattern;

/** Qué rutas son públicas y qué roles pueden usar cada una. */
final class ReglasAcceso {

    static final String ADMIN = "Administrador";
    static final String VENDEDOR = "Vendedor";
    private static final Set<String> ADMIN_VENDEDOR = Set.of(ADMIN, VENDEDOR);
    private static final Set<String> SOLO_ADMIN = Set.of(ADMIN);

    private static final Pattern USUARIO_POR_ID = Pattern.compile("^/api/usuarios/(\\d+)(/contrasena)?/?$");

    private ReglasAcceso() {}

    private static boolean lectura(HttpMethod m) {
        return HttpMethod.GET.equals(m) || HttpMethod.HEAD.equals(m);
    }

    /** Rutas que se pueden usar sin iniciar sesión. */
    static boolean esPublica(HttpMethod m, String ruta) {
        if (ruta.startsWith("/docs/") || ruta.startsWith("/swagger-ui") || ruta.startsWith("/webjars/")
                || ruta.startsWith("/v3/api-docs") || ruta.equals("/actuator/health")) {
            return true;
        }
        if (HttpMethod.POST.equals(m) && (ruta.equals("/api/usuarios/login") || ruta.equals("/api/usuarios/registro")
                || ruta.equals("/api/servicios/contacto"))) {
            return true;
        }
        if (lectura(m)) {
            if (ruta.startsWith("/api/servicios/contacto")) return false;
            return ruta.startsWith("/api/productos") || ruta.startsWith("/api/novedades")
                    || ruta.startsWith("/api/puntos-retiro") || ruta.startsWith("/api/servicios");
        }
        return false;
    }

    /**
     * Mientras la persona use la contraseña temporal (claim "cpw" en el token) solo puede cambiarla:
     * PUT /api/usuarios/{su id}/contrasena. Todo lo demás se rechaza hasta que la cambie y vuelva a iniciar sesión.
     */
    static boolean permitidaConContrasenaTemporal(HttpMethod m, String ruta, String usuarioId) {
        var propio = USUARIO_POR_ID.matcher(ruta);
        return HttpMethod.PUT.equals(m) && propio.matches() && propio.group(2) != null
                && propio.group(1).equals(usuarioId);
    }

    /**
     * Roles que pueden usar la ruta (con sesión iniciada). null = cualquier usuario con sesión.
     * @param usuarioId id que viene en el token, para permitir que cada persona vea o edite su propia cuenta
     */
    static Set<String> rolesPermitidos(HttpMethod m, String ruta, String usuarioId) {
        if (ruta.startsWith("/api/roles")) return SOLO_ADMIN;

        if (ruta.startsWith("/api/usuarios")) {
            // Renovar el propio token: cualquier persona con sesión (el servicio vuelve a verificar el token)
            if (HttpMethod.POST.equals(m) && (ruta.equals("/api/usuarios/renovar-token") || ruta.equals("/api/usuarios/renovar-token/"))) {
                return null;
            }
            var propio = USUARIO_POR_ID.matcher(ruta);
            if (propio.matches() && propio.group(1).equals(usuarioId)
                    && (lectura(m) || HttpMethod.PUT.equals(m))) {
                return null; // su propia ficha o su propia contraseña
            }
            return SOLO_ADMIN;
        }

        if (ruta.startsWith("/api/servicios/contacto")) {
            // cada persona ve sus propios mensajes y respuestas; el resto (leer todos, responder, marcar) es del equipo
            if (lectura(m) && (ruta.equals("/api/servicios/contacto/mias") || ruta.equals("/api/servicios/contacto/mias/"))) return null;
            return ADMIN_VENDEDOR;
        }
        if (ruta.startsWith("/api/productos") || ruta.startsWith("/api/novedades")
                || ruta.startsWith("/api/puntos-retiro") || ruta.startsWith("/api/servicios")) {
            return SOLO_ADMIN; // escritura del catálogo
        }

        if (ruta.startsWith("/api/pedidos")) {
            boolean listado = ruta.equals("/api/pedidos") || ruta.equals("/api/pedidos/");
            // Crear pedidos directamente es interno (Ventas lo hace sin pasar por el Gateway)
            if (HttpMethod.PATCH.equals(m) || HttpMethod.POST.equals(m) || (lectura(m) && listado)) return ADMIN_VENDEDOR;
            return null; // "mis pedidos" y ver un pedido por código (el servicio verifica que sea suyo)
        }

        if (ruta.startsWith("/api/ventas")) {
            if (HttpMethod.POST.equals(m) && ruta.startsWith("/api/ventas/checkout")) return null;
            if (lectura(m) && ruta.startsWith("/api/ventas/boleta/")) return null;
            return ADMIN_VENDEDOR;
        }

        return null;
    }
}
