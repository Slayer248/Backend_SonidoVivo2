package cl.sonidovivo.venta.config;

import cl.sonidovivo.venta.model.EstadoPago;
import cl.sonidovivo.venta.model.Venta;
import cl.sonidovivo.venta.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Ventas de ejemplo que corresponden a los pedidos PED-101 y PED-102. */
@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final VentaRepository ventaRepository;

    @Override
    public void run(String... args) {
        if (ventaRepository.count() > 0) return;
        guardar("Carlos Pérez", LocalDate.of(2026, 9, 7), 750000, "PED-101", "4242");
        guardar("María González", LocalDate.of(2026, 9, 6), 85000, "PED-102", "1881");
    }

    private void guardar(String cliente, LocalDate fecha, int total, String pedido, String ultimos4) {
        int neto = (int) Math.round(total / 1.19);
        Venta v = ventaRepository.save(Venta.builder().clienteNombre(cliente).neto(neto).iva(total - neto)
                .total(total).pedidoCodigo(pedido).tarjetaUltimos4(ultimos4)
                .estadoPago(EstadoPago.APROBADO).fecha(fecha.atTime(12, 0)).build());
        v.setNumeroBoleta(String.format("B-%06d", v.getId()));
        ventaRepository.save(v);
    }
}
