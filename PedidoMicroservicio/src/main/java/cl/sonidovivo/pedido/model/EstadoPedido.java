package cl.sonidovivo.pedido.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** En el JSON viaja con las mismas etiquetas que usa el front: "En Preparación", "Despachado", "Entregado". */
public enum EstadoPedido {
    EN_PREPARACION("En Preparación"),
    DESPACHADO("Despachado"),
    ENTREGADO("Entregado");

    private final String etiqueta;

    EstadoPedido(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @JsonValue
    public String getEtiqueta() {
        return etiqueta;
    }

    @JsonCreator
    public static EstadoPedido desde(String valor) {
        if (valor == null) return null;
        for (EstadoPedido e : values()) {
            if (e.etiqueta.equalsIgnoreCase(valor.trim()) || e.name().equalsIgnoreCase(valor.trim())) return e;
        }
        throw new IllegalArgumentException("Estado de pedido inválido: " + valor);
    }
}
