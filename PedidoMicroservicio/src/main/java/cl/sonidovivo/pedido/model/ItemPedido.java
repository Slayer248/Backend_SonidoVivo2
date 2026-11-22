package cl.sonidovivo.pedido.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/** Línea del pedido: copia del producto al momento de la compra. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPedido {

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false)
    private Integer precio;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(length = 500)
    private String img;
}
