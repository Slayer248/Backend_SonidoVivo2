package cl.sonidovivo.novedad.service;

import cl.sonidovivo.novedad.dto.NovedadRequest;
import cl.sonidovivo.novedad.model.Novedad;
import cl.sonidovivo.novedad.repository.NovedadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NovedadService {

    private final NovedadRepository novedadRepository;

    /** Más recientes primero. */
    public List<Novedad> listar(String categoria) {
        return categoria == null || categoria.isBlank()
                ? novedadRepository.findAllByOrderByFechaDescIdDesc()
                : novedadRepository.findByCategoriaIgnoreCaseOrderByFechaDescIdDesc(categoria.trim());
    }

    public Novedad obtener(Long id) {
        return novedadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Novedad no encontrada"));
    }

    public Novedad crear(NovedadRequest d) {
        validar(d);
        return novedadRepository.save(aplicar(new Novedad(), d));
    }

    public Novedad actualizar(Long id, NovedadRequest d) {
        Novedad n = obtener(id);
        validar(d);
        return novedadRepository.save(aplicar(n, d));
    }

    public void eliminar(Long id) {
        novedadRepository.delete(obtener(id));
    }

    private Novedad aplicar(Novedad n, NovedadRequest d) {
        n.setTitulo(d.titulo().trim());
        n.setCategoria(d.categoria().trim());
        n.setFecha(d.fecha() != null ? d.fecha() : LocalDate.now());
        n.setAutor(d.autor().trim());
        n.setImagen(d.imagen() == null ? null : d.imagen().trim());
        n.setResumen(d.resumen().trim());
        n.setDescripcionCompleta(d.descripcionCompleta().trim());
        return n;
    }

    private void validar(NovedadRequest d) {
        if (d.titulo() == null || d.titulo().isBlank() || d.categoria() == null || d.categoria().isBlank()
                || d.autor() == null || d.autor().isBlank() || d.resumen() == null || d.resumen().isBlank()
                || d.descripcionCompleta() == null || d.descripcionCompleta().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Título, categoría, autor, resumen y descripción completa son obligatorios");
        }
    }
}
