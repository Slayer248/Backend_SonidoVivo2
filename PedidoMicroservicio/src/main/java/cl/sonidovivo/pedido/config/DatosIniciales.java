package cl.sonidovivo.pedido.config;

import cl.sonidovivo.pedido.model.EstadoPedido;
import cl.sonidovivo.pedido.model.Pedido;
import cl.sonidovivo.pedido.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Los dos pedidos de ejemplo de src/data/pedidos.ts (PED-101 y PED-102). */
@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final PedidoRepository pedidoRepository;

    @Override
    public void run(String... args) {
        if (pedidoRepository.count() > 0) return;
        guardar("Carlos Pérez", LocalDate.of(2026, 9, 7), 750000, EstadoPedido.EN_PREPARACION);
        guardar("María González", LocalDate.of(2026, 9, 6), 85000, EstadoPedido.DESPACHADO);
    }

    private void guardar(String cliente, LocalDate fecha, int total, EstadoPedido estado) {
        int neto = (int) Math.round(total / 1.19);
        Pedido p = pedidoRepository.save(Pedido.builder().clienteNombre(cliente).fecha(fecha)
                .estado(estado).neto(neto).iva(total - neto).total(total).build());
        p.setCodigo("PED-" + (100 + p.getId()));
        pedidoRepository.save(p);
    }
}
