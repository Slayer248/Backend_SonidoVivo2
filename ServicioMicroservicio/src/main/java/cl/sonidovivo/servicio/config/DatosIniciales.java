package cl.sonidovivo.servicio.config;

import cl.sonidovivo.servicio.model.Servicio;
import cl.sonidovivo.servicio.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Servicios iniciales tomados de ServiciosPage.tsx del front. */
@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final ServicioRepository servicioRepository;

    @Override
    public void run(String... args) {
        if (servicioRepository.count() == 0) {
            servicioRepository.saveAll(List.of(
                Servicio.builder().icono("tools").titulo("Reparación y mantención").texto("Calibración de guitarras y bajos, cambio de cuerdas, ajuste de alma y reparación de equipos de audio.").build(),
                Servicio.builder().icono("speaker-fill").titulo("Arriendo de equipos").texto("Amplificación, consolas e iluminación para tocatas, eventos y ensayos.").build(),
                Servicio.builder().icono("music-note-beamed").titulo("Clases").texto("Clases de guitarra, teclado y batería para todos los niveles con profesores de la zona.").build(),
                Servicio.builder().icono("headset").titulo("Asesoría personalizada").texto("Te ayudamos a elegir el instrumento o equipo ideal según tu estilo y presupuesto.").build()
            ));
        }
    }
}
