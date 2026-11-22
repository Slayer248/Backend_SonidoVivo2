package cl.sonidovivo.gateway.security;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRETO = "secreto-de-prueba-0123456789";

    @Test
    void tokenValidoSeVerificaYEntregaLosDatos() {
        String token = JwtUtil.crear(7L, "Administrador", "admin@sonidovivo.cl", 60, SECRETO);
        Map<String, Object> claims = JwtUtil.verificar(token, SECRETO);
        assertNotNull(claims);
        assertEquals("7", claims.get("sub"));
        assertEquals("Administrador", claims.get("rol"));
    }

    @Test
    void tokenConOtroSecretoSeRechaza() {
        String token = JwtUtil.crear(7L, "Cliente", "a@a.cl", 60, SECRETO);
        assertNull(JwtUtil.verificar(token, "otro-secreto"));
    }

    @Test
    void tokenVencidoSeRechaza() {
        String token = JwtUtil.crear(7L, "Cliente", "a@a.cl", -10, SECRETO);
        assertNull(JwtUtil.verificar(token, SECRETO));
    }

    @Test
    void tokenAlteradoSeRechaza() {
        String token = JwtUtil.crear(7L, "Cliente", "a@a.cl", 60, SECRETO);
        String[] partes = token.split("\\.");
        // se reemplaza el cuerpo por otro con rol Administrador pero se conserva la firma original
        String falso = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                ("{\"sub\":\"7\",\"rol\":\"Administrador\",\"exp\":" + (System.currentTimeMillis() / 1000 + 600) + "}").getBytes());
        assertNull(JwtUtil.verificar(partes[0] + "." + falso + "." + partes[2], SECRETO));
    }

    @Test
    void textoQueNoEsTokenSeRechaza() {
        assertNull(JwtUtil.verificar("abc", SECRETO));
        assertNull(JwtUtil.verificar("a.b.c", SECRETO));
    }
}
