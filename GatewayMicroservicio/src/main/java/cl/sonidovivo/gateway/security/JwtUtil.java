package cl.sonidovivo.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JWT HS256 mínimo (sin librerías externas). Usuario lo firma al iniciar sesión y el Gateway lo verifica.
 * Ambos deben compartir el secreto (variable de entorno JWT_SECRET).
 */
public final class JwtUtil {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final String CABECERA = B64.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

    private JwtUtil() {}

    private static byte[] firmar(String datos, String secreto) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(datos.getBytes(StandardCharsets.UTF_8));
    }

    /** Crea un token con sub (id), rol y correo que vence en {@code segundos}. */
    public static String crear(Long id, String rol, String correo, long segundos, String secreto) {
        try {
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("sub", String.valueOf(id));
            claims.put("rol", rol);
            claims.put("correo", correo);
            claims.put("exp", System.currentTimeMillis() / 1000 + segundos);
            String cuerpo = B64.encodeToString(JSON.writeValueAsBytes(claims));
            String datos = CABECERA + "." + cuerpo;
            return datos + "." + B64.encodeToString(firmar(datos, secreto));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el token", e);
        }
    }

    /** Devuelve los claims si la firma es válida y no venció; si no, null. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> verificar(String token, String secreto) {
        try {
            String[] partes = token.split("\\.");
            if (partes.length != 3) return null;
            byte[] esperada = firmar(partes[0] + "." + partes[1], secreto);
            byte[] recibida = Base64.getUrlDecoder().decode(partes[2]);
            if (!MessageDigest.isEqual(esperada, recibida)) return null;
            Map<String, Object> claims = JSON.readValue(Base64.getUrlDecoder().decode(partes[1]), Map.class);
            Object exp = claims.get("exp");
            if (!(exp instanceof Number n) || n.longValue() < System.currentTimeMillis() / 1000) return null;
            return claims;
        } catch (Exception e) {
            return null;
        }
    }
}
