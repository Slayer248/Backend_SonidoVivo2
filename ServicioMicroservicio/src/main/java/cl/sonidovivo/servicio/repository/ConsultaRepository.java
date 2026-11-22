package cl.sonidovivo.servicio.repository;

import cl.sonidovivo.servicio.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findAllByOrderByFechaDesc();
    List<Consulta> findByAtendidaOrderByFechaDesc(Boolean atendida);

    /** Mensajes de una persona: los enviados con su sesión o con el correo de su cuenta. */
    List<Consulta> findByUsuarioIdOrEmailIgnoreCaseOrderByFechaDesc(Long usuarioId, String email);
}
