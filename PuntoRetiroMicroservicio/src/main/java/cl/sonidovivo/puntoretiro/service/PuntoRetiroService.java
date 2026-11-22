package cl.sonidovivo.puntoretiro.service;

import cl.sonidovivo.puntoretiro.dto.PuntoRetiroRequest;
import cl.sonidovivo.puntoretiro.model.PuntoRetiro;
import cl.sonidovivo.puntoretiro.repository.PuntoRetiroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PuntoRetiroService {

    private final PuntoRetiroRepository puntoRetiroRepository;

    /** El punto principal aparece primero. */
    public List<PuntoRetiro> listar() {
        return puntoRetiroRepository.findAllByOrderByPrincipalDescIdAsc();
    }

    public PuntoRetiro obtener(Long id) {
        return puntoRetiroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Punto de retiro no encontrado"));
    }

    @Transactional
    public PuntoRetiro crear(PuntoRetiroRequest d) {
        validar(d);
        PuntoRetiro p = aplicar(new PuntoRetiro(), d);
        quitarPrincipalAOtros(p);
        return puntoRetiroRepository.save(p);
    }

    @Transactional
    public PuntoRetiro actualizar(Long id, PuntoRetiroRequest d) {
        PuntoRetiro p = obtener(id);
        validar(d);
        aplicar(p, d);
        quitarPrincipalAOtros(p);
        return puntoRetiroRepository.save(p);
    }

    public void eliminar(Long id) {
        puntoRetiroRepository.delete(obtener(id));
    }

    /** Solo puede haber un punto principal. */
    private void quitarPrincipalAOtros(PuntoRetiro nuevo) {
        if (Boolean.TRUE.equals(nuevo.getPrincipal())) {
            puntoRetiroRepository.findAll().stream()
                    .filter(o -> Boolean.TRUE.equals(o.getPrincipal()) && !o.equals(nuevo)
                            && (nuevo.getId() == null || !o.getId().equals(nuevo.getId())))
                    .forEach(o -> { o.setPrincipal(false); puntoRetiroRepository.save(o); });
        }
    }

    private PuntoRetiro aplicar(PuntoRetiro p, PuntoRetiroRequest d) {
        p.setNombre(d.nombre().trim());
        p.setTipo(d.tipo().trim());
        p.setDireccion(d.direccion().trim());
        p.setHorario(d.horario().trim());
        p.setLat(d.lat());
        p.setLng(d.lng());
        p.setPrincipal(Boolean.TRUE.equals(d.principal()));
        return p;
    }

    private void validar(PuntoRetiroRequest d) {
        if (d.nombre() == null || d.nombre().isBlank() || d.tipo() == null || d.tipo().isBlank()
                || d.direccion() == null || d.direccion().isBlank() || d.horario() == null || d.horario().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre, tipo, dirección y horario son obligatorios");
        }
        if (d.lat() == null || d.lng() == null || d.lat() < -90 || d.lat() > 90 || d.lng() < -180 || d.lng() > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coordenadas inválidas");
        }
    }
}
