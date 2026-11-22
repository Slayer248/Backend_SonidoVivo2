package cl.sonidovivo.pedido.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código visible, ej. PED-101. */
    @Column(unique = true, length = 20)
    private String codigo;

    @Column(name = "cliente_nombre", nullable = false, length = 150)
    private String clienteNombre;

    @Column(length = 12)
    private String rut;

    @Column(length = 100)
    private String email;

    @Column(length = 200)
    private String direccion;

    @Column(name = "punto_retiro_id")
    private Long puntoRetiroId;

    /** Dueño del pedido (id de la cuenta que pagó). Null en pedidos antiguos o de prueba. */
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    /** Los precios incluyen IVA: neto + iva = total. */
    private Integer neto;
    private Integer iva;

    @Column(nullable = false)
    private Integer total;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pedido_items", joinColumns = @JoinColumn(name = "pedido_id"))
    @Builder.Default
    private List<ItemPedido> items = new ArrayList<>();
}
