package cl.sonidovivo.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

/**
 * Autenticación y autorización por rol en el punto de entrada.
 * Las reglas están en {@link ReglasAcceso}. Si el token es válido, se envían a los microservicios
 * los encabezados X-Admin-Id / X-Usuario-Id / X-Usuario-Rol (los que vengan del cliente se descartan).
 */
@Component
public class FiltroAutenticacion implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secreto;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        HttpMethod metodo = req.getMethod();
        String ruta = req.getURI().getPath();

        // Se limpian los encabezados de identidad para que el cliente no pueda falsificarlos
        ServerHttpRequest limpia = req.mutate().headers(h -> {
            h.remove("X-Admin-Id");
            h.remove("X-Usuario-Id");
            h.remove("X-Usuario-Rol");
            h.remove("X-Usuario-Correo");
        }).build();

        String auth = req.getHeaders().getFirst("Authorization");

        if (HttpMethod.OPTIONS.equals(metodo) || ReglasAcceso.esPublica(metodo, ruta)) {
            // En rutas públicas la sesión es opcional: si trae un token válido se identifica a la persona
            // (por ejemplo, para asociar un mensaje de contacto a su cuenta). Si no, sigue como anónimo.
            if (auth != null && auth.startsWith("Bearer ")) {
                Map<String, Object> opcional = JwtUtil.verificar(auth.substring(7).trim(), secreto);
                if (opcional != null) {
                    return chain.filter(exchange.mutate().request(conIdentidad(limpia, opcional)).build());
                }
            }
            return chain.filter(exchange.mutate().request(limpia).build());
        }

        if (auth == null || !auth.startsWith("Bearer ")) {
            return rechazar(exchange, HttpStatus.UNAUTHORIZED, "Debes iniciar sesión para continuar");
        }
        Map<String, Object> claims = JwtUtil.verificar(auth.substring(7).trim(), secreto);
        if (claims == null) {
            return rechazar(exchange, HttpStatus.UNAUTHORIZED, "Tu sesión expiró o no es válida. Inicia sesión nuevamente");
        }
        String id = String.valueOf(claims.get("sub"));
        String rol = String.valueOf(claims.get("rol"));

        if (Boolean.TRUE.equals(claims.get("cpw")) && !ReglasAcceso.permitidaConContrasenaTemporal(metodo, ruta, id)) {
            return rechazar(exchange, HttpStatus.FORBIDDEN, "Debes cambiar tu contraseña temporal antes de continuar");
        }

        Set<String> permitidos = ReglasAcceso.rolesPermitidos(metodo, ruta, id);
        if (permitidos != null && !permitidos.contains(rol)) {
            return rechazar(exchange, HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta acción");
        }

        return chain.filter(exchange.mutate().request(conIdentidad(limpia, claims)).build());
    }

    private static ServerHttpRequest conIdentidad(ServerHttpRequest req, Map<String, Object> claims) {
        String id = String.valueOf(claims.get("sub"));
        ServerHttpRequest.Builder b = req.mutate()
                .header("X-Admin-Id", id)
                .header("X-Usuario-Id", id)
                .header("X-Usuario-Rol", String.valueOf(claims.get("rol")));
        Object correo = claims.get("correo");
        if (correo != null) b.header("X-Usuario-Correo", String.valueOf(correo));
        return b.build();
    }

    private Mono<Void> rechazar(ServerWebExchange exchange, HttpStatus estado, String mensaje) {
        ServerHttpResponse res = exchange.getResponse();
        res.setStatusCode(estado);
        res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String json = "{\"status\":" + estado.value() + ",\"message\":\"" + mensaje + "\"}";
        DataBuffer buffer = res.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return res.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
