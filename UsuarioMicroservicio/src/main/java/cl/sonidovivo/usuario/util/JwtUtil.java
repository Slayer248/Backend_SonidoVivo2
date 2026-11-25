package cl.sonidovivo.usuario.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Firma y verifica tokens JWT HS256 (sin librerías externas). El Gateway los verifica con el mismo secreto
 * (variable de entorno JWT_SECRET).
 */
public final class JwtUtil {

    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final String CABECERA = B64.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    private static final Pattern SUB = Pattern.compile("\"sub\":\"(\\d+)\"");
    private static final Pattern EXP = Pattern.compile("\"exp\":(\\d+)");

    /** Datos que se leen de un token válido: a quién pertenece y cuándo vence (segundos desde 1970). */
    public record DatosToken(Long usuarioId, long exp) {}

    private JwtUtil() {}

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static byte[] firmar(String datos, String secreto) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(datos.getBytes(StandardCharsets.UTF_8));
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
            return datos + "." + B64.encodeToString(firmar(datos, secreto));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el token", e);
        }
    }

    /**
     * Verifica la firma y el vencimiento de un token emitido por este servicio (se usa para renovarlo).
     * Devuelve null si la firma no coincide, el formato no es válido o el token ya venció.
     * Una vez comprobada la firma el contenido es el que generó {@link #crear}, así que basta con leer sub y exp.
     */
    public static DatosToken verificar(String token, String secreto) {
        try {
            if (token == null) return null;
            String[] partes = token.split("\\.");
            if (partes.length != 3) return null;
            byte[] esperada = firmar(partes[0] + "." + partes[1], secreto);
            byte[] recibida = Base64.getUrlDecoder().decode(partes[2]);
            // Comparación en tiempo constante, para no filtrar información de la firma
            if (!MessageDigest.isEqual(esperada, recibida)) return null;

            String json = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
            Matcher sub = SUB.matcher(json);
            Matcher exp = EXP.matcher(json);
            if (!sub.find() || !exp.find()) return null;

            long vence = Long.parseLong(exp.group(1));
            if (vence < System.currentTimeMillis() / 1000) return null;
            return new DatosToken(Long.parseLong(sub.group(1)), vence);
        } catch (Exception e) {
            return null;
        }
    }
}
