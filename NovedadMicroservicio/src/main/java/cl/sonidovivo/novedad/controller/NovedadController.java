package cl.sonidovivo.novedad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import cl.sonidovivo.novedad.dto.NovedadRequest;
import cl.sonidovivo.novedad.model.Novedad;
import cl.sonidovivo.novedad.service.NovedadService;
import java.util.List;

@RestController
@RequestMapping("/api/novedades")
@RequiredArgsConstructor
@Tag(name = "Novedades", description = "Artículos de la sección Novedades")
public class NovedadController {

    private final NovedadService novedadService;

    @Operation(summary = "Listar novedades", description = "Más recientes primero. Filtro opcional por categoría.")
    @GetMapping
    public List<Novedad> listar(@RequestParam(required = false) String categoria) {
        return novedadService.listar(categoria);
    }

    @Operation(summary = "Obtener novedad por ID")
    @GetMapping("/{id}")
    public Novedad obtener(@PathVariable Long id) {
        return novedadService.obtener(id);
    }

    @Operation(summary = "Crear novedad")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Novedad crear(@RequestBody NovedadRequest datos) {
        return novedadService.crear(datos);
    }

    @Operation(summary = "Actualizar novedad")
    @PutMapping("/{id}")
    public Novedad actualizar(@PathVariable Long id, @RequestBody NovedadRequest datos) {
        return novedadService.actualizar(id, datos);
    }

    @Operation(summary = "Eliminar novedad")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        novedadService.eliminar(id);
    }
}
