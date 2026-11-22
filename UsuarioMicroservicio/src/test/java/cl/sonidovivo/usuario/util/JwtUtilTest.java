package cl.sonidovivo.usuario.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    void generaTokenDeTresPartesConLosDatosDelUsuario() {
        String token = JwtUtil.crear(3L, "Cliente", "cliente@sonidovivo.cl", 60, "secreto");
        String[] partes = token.split("\\.");
        assertEquals(3, partes.length);
        String cuerpo = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
        assertTrue(cuerpo.contains("\"sub\":\"3\""));
        assertTrue(cuerpo.contains("\"rol\":\"Cliente\""));
        assertTrue(cuerpo.contains("\"correo\":\"cliente@sonidovivo.cl\""));
        assertTrue(cuerpo.contains("\"exp\":"));
    }

    @Test
    void elClaimDeCambioDeContrasenaSoloVaCuandoCorresponde() {
        String normal = new String(Base64.getUrlDecoder().decode(JwtUtil.crear(1L, "Cliente", "a@a.cl", 60, "s").split("\\.")[1]), StandardCharsets.UTF_8);
        String temporal = new String(Base64.getUrlDecoder().decode(JwtUtil.crear(1L, "Cliente", "a@a.cl", 60, "s", true).split("\\.")[1]), StandardCharsets.UTF_8);
        assertFalse(normal.contains("cpw"));
        assertTrue(temporal.contains("\"cpw\":true"));
    }

    @Test
    void firmasDistintasConSecretosDistintos() {
        String a = JwtUtil.crear(1L, "Administrador", "a@a.cl", 60, "uno");
        String b = JwtUtil.crear(1L, "Administrador", "a@a.cl", 60, "dos");
        assertNotEquals(a.split("\\.")[2], b.split("\\.")[2]);
    }
}
