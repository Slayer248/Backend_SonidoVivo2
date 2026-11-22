package cl.sonidovivo.pedido.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

/** Cliente HTTP hacia PuntoRetiroMicroservicio. */
@Component
public class PuntoRetiroClient {

    private final WebClient webClient;

    public PuntoRetiroClient(@Value("${puntos-retiro.service.url}") String url) {
        this.webClient = WebClient.builder().baseUrl(url).build();
    }

    /** Lanza 400 si el punto de retiro no existe. */
    public void verificarExiste(Long id) {
        try {
            webClient.get().uri("/api/puntos-retiro/{id}", id)
                    .retrieve().toBodilessEntity().block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El punto de retiro seleccionado no existe");
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de puntos de retiro no está disponible");
        }
    }
}
