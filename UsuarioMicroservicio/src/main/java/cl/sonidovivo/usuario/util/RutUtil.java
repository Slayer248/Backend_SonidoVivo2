package cl.sonidovivo.usuario.util;

/** Misma lógica de validarRut() del front (utils/validaciones.ts). */
public final class RutUtil {

    private RutUtil() {}

    /** Quita puntos y espacios y deja el formato 12345678-5 en mayúsculas. */
    public static String normalizar(String rut) {
        if (rut == null) return "";
        String limpio = rut.replace(".", "").replace("-", "").replace(" ", "").toUpperCase();
        if (limpio.length() < 2) return limpio;
        return limpio.substring(0, limpio.length() - 1) + "-" + limpio.charAt(limpio.length() - 1);
    }

    public static boolean esValido(String rutIngresado) {
        String rut = normalizar(rutIngresado).replace("-", "");
        if (!rut.matches("^\\d{7,8}[0-9K]$")) return false;
        String cuerpo = rut.substring(0, rut.length() - 1);
        char dv = rut.charAt(rut.length() - 1);
        int suma = 0;
        int mult = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * mult;
            mult = mult == 7 ? 2 : mult + 1;
        }
        int resto = 11 - (suma % 11);
        char esperado = resto == 11 ? '0' : resto == 10 ? 'K' : (char) ('0' + resto);
        return dv == esperado;
    }
}
