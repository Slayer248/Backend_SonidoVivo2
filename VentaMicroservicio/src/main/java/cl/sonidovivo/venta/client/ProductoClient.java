package cl.sonidovivo.venta.client;

import cl.sonidovivo.venta.dto.ProductoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

/** Solo lectura: Ventas consulta precios y stock para calcular la boleta. */
@Component
public class ProductoClient {

    private final WebClient webClient;

    public ProductoClient(@Value("${productos.service.url}") String url) {
        this.webClient = WebClient.builder().baseUrl(url).build();
    }

    public ProductoDto obtener(Long id) {
        try {
            return webClient.get().uri("/api/productos/{id}", id)
                    .retrieve().bodyToMono(ProductoDto.class).block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto " + id + " no existe");
        } catch (WebClientRequestException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de productos no está disponible");
        }
    }
}
