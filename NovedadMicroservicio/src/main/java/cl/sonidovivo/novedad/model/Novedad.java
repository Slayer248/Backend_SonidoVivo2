package cl.sonidovivo.novedad.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "novedades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Novedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 100)
    private String autor;

    @Column(length = 500)
    private String imagen;

    @Column(nullable = false, length = 500)
    private String resumen;

    @Column(name = "descripcion_completa", nullable = false, columnDefinition = "TEXT")
    private String descripcionCompleta;
}
