package cl.sonidovivo.pedido.repository;

import cl.sonidovivo.pedido.model.EstadoPedido;
import cl.sonidovivo.pedido.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByCodigo(String codigo);
    List<Pedido> findAllByOrderByIdDesc();
    List<Pedido> findByEstadoOrderByIdDesc(EstadoPedido estado);
    List<Pedido> findByEmailIgnoreCaseOrderByIdDesc(String email);
    List<Pedido> findByUsuarioIdOrderByIdDesc(Long usuarioId);
}
