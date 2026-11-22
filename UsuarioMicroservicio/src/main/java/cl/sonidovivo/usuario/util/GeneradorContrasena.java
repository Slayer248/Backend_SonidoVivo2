package cl.sonidovivo.usuario.util;

import java.security.SecureRandom;

/** Genera contraseñas temporales legibles (sin caracteres que se confunden, como 0/O o 1/l/I). */
public final class GeneradorContrasena {

    private static final String MAYUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String MINUSCULAS = "abcdefghijkmnpqrstuvwxyz";
    private static final String DIGITOS = "23456789";
    private static final String TODOS = MAYUSCULAS + MINUSCULAS + DIGITOS;
    private static final SecureRandom AZAR = new SecureRandom();

    private GeneradorContrasena() {}

    /** Largo mínimo 8; siempre trae al menos una mayúscula, una minúscula y un dígito. */
    public static String generar(int largo) {
        if (largo < 8) throw new IllegalArgumentException("El largo mínimo es 8");
        char[] c = new char[largo];
        c[0] = al(MAYUSCULAS);
        c[1] = al(MINUSCULAS);
        c[2] = al(DIGITOS);
        for (int i = 3; i < largo; i++) c[i] = al(TODOS);
        for (int i = c.length - 1; i > 0; i--) { // mezcla para que las obligatorias no queden siempre al inicio
            int j = AZAR.nextInt(i + 1);
            char t = c[i]; c[i] = c[j]; c[j] = t;
        }
        return new String(c);
    }

    private static char al(String alfabeto) {
        return alfabeto.charAt(AZAR.nextInt(alfabeto.length()));
    }
}
