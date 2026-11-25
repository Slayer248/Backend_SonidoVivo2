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

    @Test
    void verificaUnTokenValidoYDevuelveElIdDelUsuario() {
        String token = JwtUtil.crear(7L, "Vendedor", "v@sonidovivo.cl", 60, "secreto");
        JwtUtil.DatosToken datos = JwtUtil.verificar(token, "secreto");
        assertNotNull(datos);
        assertEquals(7L, datos.usuarioId());
        assertTrue(datos.exp() > System.currentTimeMillis() / 1000);
    }

    @Test
    void rechazaTokenVencidoFirmadoConOtroSecretoOAlterado() {
        assertNull(JwtUtil.verificar(JwtUtil.crear(1L, "Cliente", "a@a.cl", -10, "secreto"), "secreto")); // vencido
        assertNull(JwtUtil.verificar(JwtUtil.crear(1L, "Cliente", "a@a.cl", 60, "otro"), "secreto"));    // otra firma

        // Cambiar el contenido (por ejemplo, el id) invalida la firma
        String[] partes = JwtUtil.crear(1L, "Cliente", "a@a.cl", 60, "secreto").split("\\.");
        String falso = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"99\",\"rol\":\"Administrador\",\"exp\":9999999999}".getBytes(StandardCharsets.UTF_8));
        assertNull(JwtUtil.verificar(partes[0] + "." + falso + "." + partes[2], "secreto"));

        assertNull(JwtUtil.verificar("no-es-un-token", "secreto"));
        assertNull(JwtUtil.verificar(null, "secreto"));
    }
}
