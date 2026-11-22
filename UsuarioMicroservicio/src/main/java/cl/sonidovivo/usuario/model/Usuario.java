package cl.sonidovivo.usuario.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** RUN normalizado: sin puntos y con guion (ej. 12345678-5). */
    @Column(nullable = false, unique = true, length = 12)
    private String run;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    /** Hash BCrypt. */
    @Column(nullable = false, length = 255)
    private String contrasena;

    @Column(name = "rol_id", nullable = false)
    private Long rolId;

    @Column(length = 80)
    private String region;

    @Column(length = 80)
    private String comuna;

    @Column(length = 200)
    private String direccion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean bloqueado = false;

    /** true mientras la persona use la contraseña temporal que se le envió por correo. */
    @Column(name = "debe_cambiar_contrasena", nullable = false)
    @Builder.Default
    private Boolean debeCambiarContrasena = false;
}
