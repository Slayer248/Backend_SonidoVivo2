package cl.sonidovivo.rol.service;

import cl.sonidovivo.rol.model.Rol;
import cl.sonidovivo.rol.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    public List<Rol> listar() {
        return rolRepository.findAllByOrderByIdAsc();
    }

    public Rol obtener(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));
    }

    public Rol crear(Rol rol) {
        if (rol.getNombre() == null || rol.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del rol es obligatorio");
        }
        String nombreLimpio = rol.getNombre().trim();
        if (rolRepository.existsByNombreIgnoreCase(nombreLimpio)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El rol '" + nombreLimpio + "' ya existe");
        }
        return rolRepository.save(Rol.builder().nombre(nombreLimpio).build());
    }
}
