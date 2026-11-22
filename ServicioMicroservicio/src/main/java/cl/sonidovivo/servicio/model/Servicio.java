package cl.sonidovivo.servicio.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "servicios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del ícono de Bootstrap Icons (ej. "tools"). */
    @Column(nullable = false, length = 50)
    private String icono;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String texto;
}
