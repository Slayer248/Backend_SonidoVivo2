package cl.sonidovivo.rol.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import cl.sonidovivo.rol.model.Rol;
import cl.sonidovivo.rol.service.RolService;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Consulta y creación de roles del sistema")
public class RolController {

    private final RolService rolService;

    @Operation(summary = "Listar roles")
    @GetMapping
    public List<Rol> listar() {
        return rolService.listar();
    }

    @Operation(summary = "Obtener rol por ID")
    @GetMapping("/{id}")
    public Rol obtener(@PathVariable Long id) {
        return rolService.obtener(id);
    }

    @Operation(summary = "Crear nuevo rol")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Rol crear(@RequestBody Rol rol) {
        return rolService.crear(rol);
    }
}
