package cl.sonidovivo.servicio.service;

import cl.sonidovivo.servicio.dto.ServicioRequest;
import cl.sonidovivo.servicio.model.Servicio;
import cl.sonidovivo.servicio.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public List<Servicio> listar() {
        return servicioRepository.findAllByOrderByIdAsc();
    }

    public Servicio obtener(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));
    }

    public Servicio crear(ServicioRequest d) {
        validar(d);
        return servicioRepository.save(Servicio.builder()
                .icono(d.icono().trim()).titulo(d.titulo().trim()).texto(d.texto().trim()).build());
    }

    public Servicio actualizar(Long id, ServicioRequest d) {
        Servicio s = obtener(id);
        validar(d);
        s.setIcono(d.icono().trim());
        s.setTitulo(d.titulo().trim());
        s.setTexto(d.texto().trim());
        return servicioRepository.save(s);
    }

    public void eliminar(Long id) {
        servicioRepository.delete(obtener(id));
    }

    private void validar(ServicioRequest d) {
        if (d.icono() == null || d.icono().isBlank() || d.titulo() == null || d.titulo().isBlank()
                || d.texto() == null || d.texto().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ícono, título y texto son obligatorios");
        }
    }
}
