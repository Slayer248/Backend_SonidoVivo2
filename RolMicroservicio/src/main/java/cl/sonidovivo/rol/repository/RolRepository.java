package cl.sonidovivo.rol.repository;

import cl.sonidovivo.rol.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolRepository extends JpaRepository<Rol, Long> {
    List<Rol> findAllByOrderByIdAsc();
    boolean existsByNombreIgnoreCase(String nombre);
}
