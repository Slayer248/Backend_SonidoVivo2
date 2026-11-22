package cl.sonidovivo.venta.repository;

import cl.sonidovivo.venta.model.EstadoPago;
import cl.sonidovivo.venta.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findAllByOrderByIdDesc();
    List<Venta> findByEstadoPagoOrderByIdDesc(EstadoPago estadoPago);
    Optional<Venta> findByNumeroBoleta(String numeroBoleta);
}
