package cl.sonidovivo.puntoretiro.repository;

import cl.sonidovivo.puntoretiro.model.PuntoRetiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PuntoRetiroRepository extends JpaRepository<PuntoRetiro, Long> {
    List<PuntoRetiro> findAllByOrderByPrincipalDescIdAsc();
}
