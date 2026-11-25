package cl.sonidovivo.usuario.service;

import cl.sonidovivo.usuario.client.RolClient;
import cl.sonidovivo.usuario.dto.RolDto;
import cl.sonidovivo.usuario.dto.UsuarioResponse;
import cl.sonidovivo.usuario.model.Usuario;
import cl.sonidovivo.usuario.repository.UsuarioRepository;
import cl.sonidovivo.usuario.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RenovarTokenTest {

    private static final String SECRETO = "secreto-de-prueba";

    private UsuarioRepository repositorio;
    private RolClient rolClient;
    private UsuarioService servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(UsuarioRepository.class);
        rolClient = mock(RolClient.class);
        servicio = new UsuarioService(repositorio, rolClient, mock(CorreoService.class));
        ReflectionTestUtils.setField(servicio, "jwtSecret", SECRETO);
        ReflectionTestUtils.setField(servicio, "jwtExpiracion", 28800L);
    }

    private Usuario usuario(Long id, Long rolId, boolean bloqueado, boolean debeCambiar) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setRolId(rolId);
        u.setCorreo("juan@sonidovivo.cl");
        u.setBloqueado(bloqueado);
        u.setDebeCambiarContrasena(debeCambiar);
        return u;
    }

    @Test
    void conTokenVigenteEntregaUnoNuevoConElRolActual() {
        // El token se emitió cuando era Cliente; desde entonces el administrador le cambió el rol a Vendedor
        String token = JwtUtil.crear(5L, "Cliente", "juan@sonidovivo.cl", 60, SECRETO);
        when(repositorio.findById(5L)).thenReturn(Optional.of(usuario(5L, 2L, false, false)));
        when(rolClient.obtenerPorId(2L)).thenReturn(new RolDto(2L, "Vendedor"));

        UsuarioResponse r = servicio.renovarToken("Bearer " + token);

        JwtUtil.DatosToken nuevo = JwtUtil.verificar(r.token(), SECRETO);
        assertNotNull(nuevo);
        assertEquals(5L, nuevo.usuarioId());
        assertEquals("Vendedor", r.rol());
        // El nuevo vence en ~8 horas, bastante después que el original (60 segundos)
        assertTrue(nuevo.exp() > System.currentTimeMillis() / 1000 + 28000);
    }

    @Test
    void sinTokenOConTokenVencidoResponde401() {
        String vencido = JwtUtil.crear(5L, "Cliente", "juan@sonidovivo.cl", -10, SECRETO);
        for (String auth : new String[]{null, "Bearer " + vencido, "Bearer basura"}) {
            ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> servicio.renovarToken(auth));
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        }
        verifyNoInteractions(repositorio);
    }

    @Test
    void unaCuentaBloqueadaNoPuedeRenovar() {
        String token = JwtUtil.crear(5L, "Cliente", "juan@sonidovivo.cl", 60, SECRETO);
        when(repositorio.findById(5L)).thenReturn(Optional.of(usuario(5L, 3L, true, false)));

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> servicio.renovarToken("Bearer " + token));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
    }

    @Test
    void conContrasenaTemporalNoPuedeRenovar() {
        String token = JwtUtil.crear(5L, "Cliente", "juan@sonidovivo.cl", 60, SECRETO, true);
        when(repositorio.findById(5L)).thenReturn(Optional.of(usuario(5L, 3L, false, true)));

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> servicio.renovarToken("Bearer " + token));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
    }
}
