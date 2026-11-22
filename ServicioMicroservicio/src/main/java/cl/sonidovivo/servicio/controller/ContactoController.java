package cl.sonidovivo.servicio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import cl.sonidovivo.servicio.dto.*;
import cl.sonidovivo.servicio.model.Consulta;
import cl.sonidovivo.servicio.service.ConsultaService;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/servicios/contacto")
@RequiredArgsConstructor
@Tag(name = "Contacto", description = "Formulario de contacto: consultas de clientes")
public class ContactoController {

    private final ConsultaService consultaService;

    @Operation(summary = "Enviar consulta", description = "Registra el mensaje del formulario de contacto.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Consulta enviar(@RequestBody ConsultaRequest datos,
                           @RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId) {
        return consultaService.registrar(datos, aLong(usuarioId));
    }

    @Operation(summary = "Mis consultas",
            description = "Mensajes de la persona con sesión iniciada y las respuestas que recibió.")
    @GetMapping("/mias")
    public List<Consulta> mias(@RequestHeader(value = "X-Usuario-Id", required = false) String usuarioId,
                               @RequestHeader(value = "X-Usuario-Correo", required = false) String correo) {
        Long id = aLong(usuarioId);
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión para ver tus consultas");
        }
        return consultaService.listarDe(id, correo);
    }

    @Operation(summary = "Listar consultas", description = "Para el panel de administración. Filtro opcional por estado de atención.")
    @GetMapping
    public List<Consulta> listar(@RequestParam(required = false) Boolean atendida) {
        return consultaService.listar(atendida);
    }

    @Operation(summary = "Obtener consulta por ID")
    @GetMapping("/{id}")
    public Consulta obtener(@PathVariable Long id) {
        return consultaService.obtener(id);
    }

    @Operation(summary = "Responder consulta", description = "Guarda la respuesta y marca la consulta como atendida.")
    @PutMapping("/{id}/respuesta")
    public Consulta responder(@PathVariable Long id, @RequestBody RespuestaRequest datos) {
        return consultaService.responder(id, datos.respuesta());
    }

    @Operation(summary = "Marcar consulta como atendida / pendiente")
    @PatchMapping("/{id}/atendida")
    public Consulta marcarAtendida(@PathVariable Long id, @RequestBody AtendidaRequest datos) {
        return consultaService.marcarAtendida(id, datos.atendida());
    }

    private static Long aLong(String valor) {
        try {
            return valor == null || valor.isBlank() ? null : Long.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
