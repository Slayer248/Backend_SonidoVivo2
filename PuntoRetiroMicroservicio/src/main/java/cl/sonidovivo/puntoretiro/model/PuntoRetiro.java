package cl.sonidovivo.puntoretiro.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "puntos_retiro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuntoRetiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    /** Ej: "Tienda" o "Punto de retiro". */
    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false, length = 80)
    private String horario;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(nullable = false)
    @Builder.Default
    private Boolean principal = false;
}
