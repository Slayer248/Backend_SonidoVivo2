package cl.sonidovivo.pedido.client;

import cl.sonidovivo.pedido.dto.ProductoDto;
import cl.sonidovivo.pedido.dto.StockRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

/** Cliente HTTP hacia ProductoMicroservicio. */
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
            throw noDisponible();
        }
    }

    public void descontarStock(Long id, int cantidad) {
        try {
            webClient.patch().uri("/api/productos/{id}/stock/descuento", id)
                    .bodyValue(new StockRequest(cantidad))
                    .retrieve().toBodilessEntity().block();
        } catch (WebClientResponseException.Conflict e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para el producto " + id);
        } catch (WebClientResponseException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto " + id + " no existe");
        } catch (WebClientRequestException e) {
            throw noDisponible();
        }
    }

    /** Revierte un descuento (compensación si el pedido falla a la mitad). */
    public void reponerStock(Long id, int cantidad) {
        try {
            webClient.patch().uri("/api/productos/{id}/stock/reposicion", id)
                    .bodyValue(new StockRequest(cantidad))
                    .retrieve().toBodilessEntity().block();
        } catch (RuntimeException ignorado) {
            // Mejor esfuerzo: no ocultar el error original del pedido.
        }
    }

    private ResponseStatusException noDisponible() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de productos no está disponible");
    }
}
