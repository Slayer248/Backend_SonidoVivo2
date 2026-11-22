package cl.sonidovivo.usuario.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Firma tokens JWT HS256 (sin librerías externas). El Gateway los verifica con el mismo secreto
 * (variable de entorno JWT_SECRET).
 */
public final class JwtUtil {

    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final String CABECERA = B64.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

    private JwtUtil() {}

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** Crea un token con sub (id), rol y correo que vence en {@code segundos}. */
    public static String crear(Long id, String rol, String correo, long segundos, String secreto) {
        return crear(id, rol, correo, segundos, secreto, false);
    }

    /**
     * Igual que el anterior; si {@code cambiarContrasena} es true agrega el claim "cpw": el Gateway solo deja
     * usar la ruta de cambio de contraseña mientras el token lo lleve.
     */
    public static String crear(Long id, String rol, String correo, long segundos, String secreto, boolean cambiarContrasena) {
        try {
            long exp = System.currentTimeMillis() / 1000 + segundos;
            String json = "{\"sub\":\"" + id + "\",\"rol\":\"" + esc(rol) + "\",\"correo\":\"" + esc(correo)
                    + "\"" + (cambiarContrasena ? ",\"cpw\":true" : "") + ",\"exp\":" + exp + "}";
            String datos = CABECERA + "." + B64.encodeToString(json.getBytes(StandardCharsets.UTF_8));
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return datos + "." + B64.encodeToString(mac.doFinal(datos.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el token", e);
        }
    }
}
