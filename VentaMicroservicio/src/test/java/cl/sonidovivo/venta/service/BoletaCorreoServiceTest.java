package cl.sonidovivo.venta.service;

import cl.sonidovivo.venta.dto.LineaBoleta;
import cl.sonidovivo.venta.model.EstadoPago;
import cl.sonidovivo.venta.model.Venta;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoletaCorreoServiceTest {

    private Venta venta(String cliente) {
        return Venta.builder().numeroBoleta("B-000007").pedidoCodigo("PED-107").clienteNombre(cliente)
                .rut("22222222-2").email("cliente@sonidovivo.cl").neto(630252).iva(119748).total(750000)
                .tarjetaUltimos4("1111").estadoPago(EstadoPago.APROBADO)
                .fecha(LocalDateTime.of(2026, 11, 20, 13, 30)).build();
    }

    @Test
    void laBoletaIncluyeLosDatosDeLaCompra() {
        String html = BoletaCorreoService.html(venta("Juan Pérez"),
                List.of(new LineaBoleta("Teclado Roland XPS-10", 2, 375000)), "Los Aromos 1234");
        assertTrue(html.contains("B-000007"));
        assertTrue(html.contains("PED-107"));
        assertTrue(html.contains("Teclado Roland XPS-10"));
        assertTrue(html.contains("$750.000")); // subtotal y total
        assertTrue(html.contains("$630.252"));
        assertTrue(html.contains("terminada en 1111"));
        assertTrue(html.contains("Los Aromos 1234"));
        assertFalse(html.contains("4111")); // nunca el número completo de la tarjeta
    }

    @Test
    void escapaElTextoQueEscribeElCliente() {
        String html = BoletaCorreoService.html(venta("<script>alert(1)</script>"), List.of(), null);
        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("&lt;script&gt;"));
    }
}
