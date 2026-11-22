package cl.sonidovivo.novedad.repository;

import cl.sonidovivo.novedad.model.Novedad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NovedadRepository extends JpaRepository<Novedad, Long> {
    List<Novedad> findAllByOrderByFechaDescIdDesc();
    List<Novedad> findByCategoriaIgnoreCaseOrderByFechaDescIdDesc(String categoria);
}
