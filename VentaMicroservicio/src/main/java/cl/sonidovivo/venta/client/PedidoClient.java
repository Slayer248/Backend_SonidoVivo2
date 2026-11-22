package cl.sonidovivo.venta.client;

import cl.sonidovivo.venta.dto.PedidoDto;
import cl.sonidovivo.venta.dto.PedidoRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

/** Cliente HTTP hacia PedidoMicroservicio. */
@Component
public class PedidoClient {

    private final WebClient webClient;

    public PedidoClient(@Value("${pedidos.service.url}") String url) {
        this.webClient = WebClient.builder().baseUrl(url).build();
    }

    public PedidoDto crear(PedidoRequestDto datos) {
        try {
            return webClient.post().uri("/api/pedidos").bodyValue(datos)
                    .retrieve().bodyToMono(PedidoDto.class).block();
        } catch (WebClientResponseException.Conflict e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para completar el pedido");
        } catch (WebClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "El servicio de pedidos rechazó la solicitud (" + e.getStatusCode().value() + ")");
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de pedidos no está disponible");
        }
    }
}
