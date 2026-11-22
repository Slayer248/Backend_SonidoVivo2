package cl.sonidovivo.servicio.service;

import cl.sonidovivo.servicio.dto.ConsultaRequest;
import cl.sonidovivo.servicio.model.Consulta;
import cl.sonidovivo.servicio.repository.ConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ConsultaService {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final ConsultaRepository consultaRepository;

    public Consulta registrar(ConsultaRequest d, Long usuarioId) {
        if (d.nombre() == null || d.nombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe ingresar su nombre");
        }
        if (d.email() == null || d.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe ingresar su correo electrónico");
        }
        if (!EMAIL.matcher(d.email().trim()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo electrónico no tiene un formato válido");
        }
        if (d.mensaje() == null || d.mensaje().trim().length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mensaje debe tener al menos 10 caracteres");
        }
        return consultaRepository.save(Consulta.builder()
                .nombre(d.nombre().trim())
                .email(d.email().trim())
                .mensaje(d.mensaje().trim())
                .fecha(LocalDateTime.now())
                .atendida(false)
                .usuarioId(usuarioId)
                .build());
    }

    public List<Consulta> listar(Boolean atendida) {
        return atendida == null
                ? consultaRepository.findAllByOrderByFechaDesc()
                : consultaRepository.findByAtendidaOrderByFechaDesc(atendida);
    }

    public Consulta obtener(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consulta no encontrada"));
    }

    public Consulta marcarAtendida(Long id, Boolean atendida) {
        Consulta c = obtener(id);
        c.setAtendida(Boolean.TRUE.equals(atendida));
        return consultaRepository.save(c);
    }

    /** Guarda la respuesta del equipo y deja la consulta como atendida. */
    public Consulta responder(Long id, String respuesta) {
        if (respuesta == null || respuesta.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe escribir una respuesta");
        }
        if (respuesta.trim().length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La respuesta no puede superar los 1000 caracteres");
        }
        Consulta c = obtener(id);
        c.setRespuesta(respuesta.trim());
        c.setFechaRespuesta(LocalDateTime.now());
        c.setAtendida(true);
        return consultaRepository.save(c);
    }

    /** Consultas de la persona con sesión iniciada (por su id o por el correo de su cuenta). */
    public List<Consulta> listarDe(Long usuarioId, String correo) {
        return consultaRepository.findByUsuarioIdOrEmailIgnoreCaseOrderByFechaDesc(
                usuarioId, correo == null || correo.isBlank() ? "" : correo.trim());
    }
}
