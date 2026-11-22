package cl.sonidovivo.usuario.client;

import cl.sonidovivo.usuario.dto.RolDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Cliente HTTP hacia RolMicroservicio. */
@Component
public class RolClient {

    private final WebClient webClient;

    public RolClient(@Value("${roles.service.url}") String rolesUrl) {
        this.webClient = WebClient.builder().baseUrl(rolesUrl).build();
    }

    public RolDto obtenerPorId(Long rolId) {
        try {
            return webClient.get().uri("/api/roles/{id}", rolId)
                    .retrieve().bodyToMono(RolDto.class).block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol seleccionado no existe");
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de roles no está disponible");
        }
    }

    public RolDto obtenerPorNombre(String nombre) {
        return listar().stream()
                .filter(r -> r.nombre().equalsIgnoreCase(nombre)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "No existe el rol '" + nombre + "' en el servicio de roles"));
    }

    public Map<Long, String> obtenerMapaRoles() {
        return listar().stream().collect(Collectors.toMap(RolDto::id, RolDto::nombre));
    }

    private List<RolDto> listar() {
        try {
            List<RolDto> roles = webClient.get().uri("/api/roles").retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<RolDto>>() {}).block();
            return roles == null ? List.of() : roles;
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de roles no está disponible");
        }
    }
}
