package cl.sonidovivo.usuario.repository;

import cl.sonidovivo.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);
    boolean existsByRun(String run);
    boolean existsByRunAndIdNot(String run, Long id);
    List<Usuario> findAllByOrderByIdAsc();
}
