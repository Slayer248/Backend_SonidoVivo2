package cl.sonidovivo.puntoretiro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import cl.sonidovivo.puntoretiro.dto.PuntoRetiroRequest;
import cl.sonidovivo.puntoretiro.model.PuntoRetiro;
import cl.sonidovivo.puntoretiro.service.PuntoRetiroService;
import java.util.List;

@RestController
@RequestMapping("/api/puntos-retiro")
@RequiredArgsConstructor
@Tag(name = "Puntos de retiro", description = "Sucursales y puntos de retiro de pedidos")
public class PuntoRetiroController {

    private final PuntoRetiroService puntoRetiroService;

    @Operation(summary = "Listar puntos de retiro", description = "El punto principal aparece primero.")
    @GetMapping
    public List<PuntoRetiro> listar() {
        return puntoRetiroService.listar();
    }

    @Operation(summary = "Obtener punto de retiro por ID")
    @GetMapping("/{id}")
    public PuntoRetiro obtener(@PathVariable Long id) {
        return puntoRetiroService.obtener(id);
    }

    @Operation(summary = "Crear punto de retiro")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PuntoRetiro crear(@RequestBody PuntoRetiroRequest datos) {
        return puntoRetiroService.crear(datos);
    }

    @Operation(summary = "Actualizar punto de retiro")
    @PutMapping("/{id}")
    public PuntoRetiro actualizar(@PathVariable Long id, @RequestBody PuntoRetiroRequest datos) {
        return puntoRetiroService.actualizar(id, datos);
    }

    @Operation(summary = "Eliminar punto de retiro")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        puntoRetiroService.eliminar(id);
    }
}
