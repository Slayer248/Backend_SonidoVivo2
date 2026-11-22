package cl.sonidovivo.venta.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Registro de un intento de compra. Nunca se guarda el número completo de la tarjeta. */
@Entity
@Table(name = "ventas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Número de boleta, ej. B-000001 (solo ventas aprobadas). */
    @Column(name = "numero_boleta", unique = true, length = 20)
    private String numeroBoleta;

    /** Pedido asociado, ej. PED-101. */
    @Column(name = "pedido_codigo", length = 20)
    private String pedidoCodigo;

    @Column(name = "cliente_nombre", nullable = false, length = 150)
    private String clienteNombre;

    @Column(length = 12)
    private String rut;

    @Column(length = 100)
    private String email;

    @Column(nullable = false)
    private Integer neto;

    @Column(nullable = false)
    private Integer iva;

    @Column(nullable = false)
    private Integer total;

    @Column(name = "tarjeta_ultimos4", length = 4)
    private String tarjetaUltimos4;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false, length = 15)
    private EstadoPago estadoPago;

    /** Motivo del rechazo, si lo hubo. */
    @Column(length = 255)
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
