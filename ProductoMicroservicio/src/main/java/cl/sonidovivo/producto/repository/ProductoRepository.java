package cl.sonidovivo.producto.repository;

import cl.sonidovivo.producto.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findAllByOrderByIdAsc();

    /** Descuento atómico: solo actúa si hay stock suficiente. Retorna filas afectadas (0 = sin stock). */
    @Modifying
    @Query("update Producto p set p.stock = p.stock - :cantidad where p.id = :id and p.stock >= :cantidad")
    int descontarStock(@Param("id") Long id, @Param("cantidad") int cantidad);

    @Modifying
    @Query("update Producto p set p.stock = p.stock + :cantidad where p.id = :id")
    int reponerStock(@Param("id") Long id, @Param("cantidad") int cantidad);
}
