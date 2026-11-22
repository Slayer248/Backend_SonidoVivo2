package cl.sonidovivo.servicio.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Mensaje enviado desde el formulario de contacto. */
@Entity
@Table(name = "consultas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 1000)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    @Builder.Default
    private Boolean atendida = false;

    /** Cuenta que envió el mensaje, si tenía la sesión iniciada. */
    @Column(name = "usuario_id")
    private Long usuarioId;

    /** Respuesta escrita desde el panel de administración. */
    @Column(length = 1000)
    private String respuesta;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;
}
