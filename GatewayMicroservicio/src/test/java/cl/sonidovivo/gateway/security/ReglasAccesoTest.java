package cl.sonidovivo.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.junit.jupiter.api.Assertions.*;

class ReglasAccesoTest {

    @Test
    void rutasPublicas() {
        assertTrue(ReglasAcceso.esPublica(HttpMethod.POST, "/api/usuarios/login"));
        assertTrue(ReglasAcceso.esPublica(HttpMethod.POST, "/api/usuarios/registro"));
        assertTrue(ReglasAcceso.esPublica(HttpMethod.GET, "/api/productos"));
        assertTrue(ReglasAcceso.esPublica(HttpMethod.GET, "/api/puntos-retiro"));
        assertTrue(ReglasAcceso.esPublica(HttpMethod.POST, "/api/servicios/contacto"));
        assertTrue(ReglasAcceso.esPublica(HttpMethod.GET, "/docs/productos/v3/api-docs"));
    }

    @Test
    void rutasQueExigenSesion() {
        assertFalse(ReglasAcceso.esPublica(HttpMethod.GET, "/api/usuarios"));
        assertFalse(ReglasAcceso.esPublica(HttpMethod.GET, "/api/pedidos"));
        assertFalse(ReglasAcceso.esPublica(HttpMethod.GET, "/api/servicios/contacto")); // consultas privadas
        assertFalse(ReglasAcceso.esPublica(HttpMethod.POST, "/api/productos"));
        assertFalse(ReglasAcceso.esPublica(HttpMethod.POST, "/api/ventas/checkout"));
    }

    @Test
    void usuariosYRolesSoloParaAdministrador() {
        assertEquals(java.util.Set.of("Administrador"), ReglasAcceso.rolesPermitidos(HttpMethod.GET, "/api/usuarios", "5"));
        assertEquals(java.util.Set.of("Administrador"), ReglasAcceso.rolesPermitidos(HttpMethod.GET, "/api/roles", "5"));
        assertEquals(java.util.Set.of("Administrador"), ReglasAcceso.rolesPermitidos(HttpMethod.PATCH, "/api/usuarios/5/rol", "5"));
    }

    @Test
    void cadaPersonaPuedeVerYEditarSuPropiaCuenta() {
        assertNull(ReglasAcceso.rolesPermitidos(HttpMethod.GET, "/api/usuarios/5", "5"));
        assertNull(ReglasAcceso.rolesPermitidos(HttpMethod.PUT, "/api/usuarios/5", "5"));
        assertNull(ReglasAcceso.rolesPermitidos(HttpMethod.PUT, "/api/usuarios/5/contrasena", "5"));
        // la de otra persona, no
        assertEquals(java.util.Set.of("Administrador"), ReglasAcceso.rolesPermitidos(HttpMethod.GET, "/api/usuarios/6", "5"));
        // y no puede bloquearse ni cambiarse el rol por esta vía
        assertEquals(java.util.Set.of("Administrador"), ReglasAcceso.rolesPermitidos(HttpMethod.PATCH, "/api/usuarios/5/rol", "5"));
        assertEquals(java.util.Set.of("Administrador"), ReglasAcceso.rolesPermitidos(HttpMethod.PATCH, "/api/usuarios/5/bloqueo", "5"));
    }

    @Test
    void cualquierPersonaConSesionPuedeRenovarSuToken() {
        assertFalse(ReglasAcceso.esPublica(HttpMethod.POST, "/api/usuarios/renovar-token")); // exige token
        assertNull(ReglasAcceso.rolesPermitidos(HttpMethod.POST, "/api/usuarios/renovar-token", "5"));
        // con contraseña temporal solo se permite cambiarla, no renovar
        assertFalse(ReglasAcceso.permitidaConContrasenaTemporal(HttpMethod.POST, "/api/usuarios/renovar-token", "5"));
    }

    @Test
    void catalogoSoloSeEscribeComoAdministrador() {
        for (String ruta : new String[]{"/api/productos", "/api/productos/3", "/api/novedades", "/api/servicios", "/api/puntos-retiro"}) {
            assertEquals(java.util.Set.of("Administrador"), ReglasAcceso.rolesPermitidos(HttpMethod.POST, ruta, "1"), ruta);
            assertEquals(java.util.Set.of("Administrador"), ReglasAcceso.rolesPermitidos(HttpMethod.DELETE, ruta, "1"), ruta);
        }
    }

    @Test
    void pedidosYVentasSegunRol() {
        var adminVendedor = java.util.Set.of("Administrador", "Vendedor");
        assertEquals(adminVendedor, ReglasAcceso.rolesPermitidos(HttpMethod.GET, "/api/pedidos", "1"));
        assertEquals(adminVendedor, ReglasAcceso.rolesPermitidos(HttpMethod.PATCH, "/api/pedidos/PED-1/estado", "1"));
        assertNull(ReglasAcceso.rolesPermitidos(HttpMethod.GET, "/api/pedidos/PED-1", "1"));
        assertNull(ReglasAcceso.rolesPermitidos(HttpMethod.GET, "/api/pedidos/mios", "1"));
        // crear pedidos directo (sin pasar por el checkout) no es para clientes
        assertEquals(adminVendedor, ReglasAcceso.rolesPermitidos(HttpMethod.POST, "/api/pedidos", "1"));
        assertNull(ReglasAcceso.rolesPermitidos(HttpMethod.POST, "/api/ventas/checkout", "1"));
        assertEquals(adminVendedor, ReglasAcceso.rolesPermitidos(HttpMethod.GET, "/api/ventas", "1"));
        assertEquals(adminVendedor, ReglasAcceso.rolesPermitidos(HttpMethod.GET, "/api/servicios/contacto", "1"));
    }

    @Test
    void consultasDeContacto() {
        var adminVendedor = java.util.Set.of("Administrador", "Vendedor");
        // cada persona con sesión ve sus mensajes
        assertNull(ReglasAcceso.rolesPermitidos(HttpMethod.GET, "/api/servicios/contacto/mias", "7"));
        assertFalse(ReglasAcceso.esPublica(HttpMethod.GET, "/api/servicios/contacto/mias"));
        // leer todas, responder y marcar atendida es del equipo
        assertEquals(adminVendedor, ReglasAcceso.rolesPermitidos(HttpMethod.GET, "/api/servicios/contacto", "7"));
        assertEquals(adminVendedor, ReglasAcceso.rolesPermitidos(HttpMethod.PUT, "/api/servicios/contacto/3/respuesta", "7"));
        assertEquals(adminVendedor, ReglasAcceso.rolesPermitidos(HttpMethod.PATCH, "/api/servicios/contacto/3/atendida", "7"));
    }

    @Test
    void conContrasenaTemporalSoloSePuedeCambiarLaContrasena() {
        // su propia contraseña: sí
        assertTrue(ReglasAcceso.permitidaConContrasenaTemporal(HttpMethod.PUT, "/api/usuarios/5/contrasena", "5"));
        // la de otra persona, su ficha, productos, pedidos o consultas: no
        assertFalse(ReglasAcceso.permitidaConContrasenaTemporal(HttpMethod.PUT, "/api/usuarios/6/contrasena", "5"));
        assertFalse(ReglasAcceso.permitidaConContrasenaTemporal(HttpMethod.PUT, "/api/usuarios/5", "5"));
        assertFalse(ReglasAcceso.permitidaConContrasenaTemporal(HttpMethod.GET, "/api/usuarios/5/contrasena", "5"));
        assertFalse(ReglasAcceso.permitidaConContrasenaTemporal(HttpMethod.POST, "/api/ventas/checkout", "5"));
        assertFalse(ReglasAcceso.permitidaConContrasenaTemporal(HttpMethod.GET, "/api/pedidos/mios", "5"));
        assertFalse(ReglasAcceso.permitidaConContrasenaTemporal(HttpMethod.GET, "/api/servicios/contacto/mias", "5"));
    }
}
