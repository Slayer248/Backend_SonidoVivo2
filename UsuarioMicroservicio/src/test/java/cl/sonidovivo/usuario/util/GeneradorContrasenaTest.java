package cl.sonidovivo.usuario.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GeneradorContrasenaTest {

    @Test
    void tieneElLargoPedidoYMezclaDeCaracteres() {
        for (int i = 0; i < 200; i++) {
            String c = GeneradorContrasena.generar(10);
            assertEquals(10, c.length());
            assertTrue(c.chars().anyMatch(Character::isUpperCase), c);
            assertTrue(c.chars().anyMatch(Character::isLowerCase), c);
            assertTrue(c.chars().anyMatch(Character::isDigit), c);
            assertTrue(c.matches("[A-Za-z0-9]+"), c);
            assertFalse(c.matches(".*[0OIl1].*"), c); // sin caracteres que se confunden
        }
    }

    @Test
    void noRepiteContrasenas() {
        Set<String> vistas = new HashSet<>();
        for (int i = 0; i < 500; i++) vistas.add(GeneradorContrasena.generar(10));
        assertEquals(500, vistas.size());
    }

    @Test
    void rechazaLargosCortos() {
        assertThrows(IllegalArgumentException.class, () -> GeneradorContrasena.generar(6));
    }
}
