package cl.sonidovivo.servicio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import cl.sonidovivo.servicio.dto.ServicioRequest;
import cl.sonidovivo.servicio.model.Servicio;
import cl.sonidovivo.servicio.service.ServicioService;
import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
@Tag(name = "Servicios", description = "Servicios que ofrece la tienda")
public class ServicioController {

    private final ServicioService servicioService;

    @Operation(summary = "Listar servicios")
    @GetMapping
    public List<Servicio> listar() {
        return servicioService.listar();
    }

    @Operation(summary = "Obtener servicio por ID")
    @GetMapping("/{id}")
    public Servicio obtener(@PathVariable Long id) {
        return servicioService.obtener(id);
    }

    @Operation(summary = "Crear servicio")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Servicio crear(@RequestBody ServicioRequest datos) {
        return servicioService.crear(datos);
    }

    @Operation(summary = "Actualizar servicio")
    @PutMapping("/{id}")
    public Servicio actualizar(@PathVariable Long id, @RequestBody ServicioRequest datos) {
        return servicioService.actualizar(id, datos);
    }

    @Operation(summary = "Eliminar servicio")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicioService.eliminar(id);
    }
}
