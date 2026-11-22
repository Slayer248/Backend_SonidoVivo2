package cl.sonidovivo.novedad.config;

import cl.sonidovivo.novedad.model.Novedad;
import cl.sonidovivo.novedad.repository.NovedadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/** Artículos iniciales tomados de src/data/novedades.ts del front. */
@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final NovedadRepository novedadRepository;

    @Override
    public void run(String... args) {
        if (novedadRepository.count() == 0) {
            novedadRepository.saveAll(List.of(
                Novedad.builder().titulo("Cómo cuidar tus guitarras en invierno").categoria("Consejos").fecha(LocalDate.of(2026, 9, 5))
                        .autor("Equipo Técnico Sonido Vivo").imagen("https://images.unsplash.com/photo-1510915361894-db8b60106cb1?auto=format&fit=crop&w=800&q=80")
                        .resumen("Los cambios de humedad y temperatura afectan la madera. Te contamos cómo protegerla.")
                        .descripcionCompleta("Guarda tu guitarra en su estuche cuando no la uses, evita dejarla cerca de estufas y usa un humidificador de estuche. Revisa la afinación con más frecuencia y limpia el diapasón con aceite de limón una vez al mes.").build(),
                Novedad.builder().titulo("Llegaron los nuevos teclados Roland").categoria("Lanzamientos").fecha(LocalDate.of(2026, 9, 20))
                        .autor("Sonido Vivo").imagen("https://images.unsplash.com/photo-1520523839897-bd0b52f945a0?auto=format&fit=crop&w=800&q=80")
                        .resumen("Ya están disponibles en tienda los nuevos modelos de la serie XPS.")
                        .descripcionCompleta("Los nuevos teclados incorporan más sonidos, conexión USB para grabar en el computador y un diseño más liviano, ideal para músicos que tocan en vivo. Ven a probarlos a nuestra tienda principal.").build(),
                Novedad.builder().titulo("5 pedales esenciales para empezar").categoria("Guías").fecha(LocalDate.of(2026, 10, 2))
                        .autor("Equipo Técnico Sonido Vivo").imagen("https://images.unsplash.com/photo-1564186763535-ebb21ef5277f?auto=format&fit=crop&w=800&q=80")
                        .resumen("Distorsión, delay, chorus, afinador y compresor: qué hace cada uno.")
                        .descripcionCompleta("Si estás armando tu primer pedalboard, parte por un afinador y una distorsión. Luego suma un delay para dar profundidad, un chorus para texturas limpias y un compresor para emparejar la dinámica.").build(),
                Novedad.builder().titulo("Taller gratuito de mantención de baterías").categoria("Eventos").fecha(LocalDate.of(2026, 10, 15))
                        .autor("Sonido Vivo").imagen("https://images.unsplash.com/photo-1519892300165-cb5542fb47c7?auto=format&fit=crop&w=800&q=80")
                        .resumen("Aprende a cambiar parches y afinar tu batería en nuestro taller de noviembre.")
                        .descripcionCompleta("El taller será el primer sábado de noviembre en nuestra tienda principal de Viña del Mar. Los cupos son limitados; inscríbete escribiéndonos desde la página de Servicios.").build(),
                Novedad.builder().titulo("Llegó la batería electrónica Alesis Nitro Max").categoria("Lanzamientos").fecha(LocalDate.of(2026, 10, 28))
                        .autor("Sonido Vivo").imagen("https://images.unsplash.com/photo-1457523054379-8d03ab9fc2aa?auto=format&fit=crop&w=800&q=80")
                        .resumen("Practica todos los días sin molestar a los vecinos: ya tenemos baterías electrónicas en tienda.")
                        .descripcionCompleta("La Alesis Nitro Max tiene parches de malla, que se sienten más parecidos a una batería acústica y hacen muy poco ruido al golpearlos. Conéctale audífonos y practica a cualquier hora, o enchúfala al computador para grabar. Es ideal para departamentos y para quienes recién empiezan. Ven a probarla a nuestra tienda principal.").build(),
                Novedad.builder().titulo("¿Teclado o piano digital? Cómo elegir tu primer instrumento").categoria("Guías").fecha(LocalDate.of(2026, 11, 5))
                        .autor("Equipo Técnico Sonido Vivo").imagen("https://images.unsplash.com/photo-1567787609897-efa3625dd22d?auto=format&fit=crop&w=800&q=80")
                        .resumen("Comparamos el Casio CT-S300 y el Yamaha P-145 para que elijas según tu objetivo.")
                        .descripcionCompleta("Si buscas explorar sonidos, tocar canciones con ritmos de acompañamiento y llevar el instrumento a todas partes, un teclado portátil como el Casio CT-S300 es la mejor puerta de entrada. Si tu meta es aprender piano en serio, conviene un piano digital como el Yamaha P-145: sus 88 teclas con peso imitan la sensación de un piano acústico y te preparan para tocar cualquier repertorio. ¿Dudas? Pruébalos ambos en tienda.").build(),
                Novedad.builder().titulo("Arma tu home studio: micrófono y audífonos para empezar").categoria("Consejos").fecha(LocalDate.of(2026, 11, 12))
                        .autor("Equipo Técnico Sonido Vivo").imagen("https://images.unsplash.com/photo-1737885197946-6d9d79dade89?auto=format&fit=crop&w=800&q=80")
                        .resumen("Con un buen micrófono y unos audífonos de estudio ya puedes grabar tus primeras canciones en casa.")
                        .descripcionCompleta("Para la voz, un micrófono dinámico como el Shure SM58 es resistente y capta poco ruido de la habitación, algo clave cuando grabas en una pieza sin aislamiento. Para escuchar lo que grabas, los audífonos de estudio Audio-Technica ATH-M50x entregan un sonido fiel, sin realzar los graves, para que tus mezclas suenen bien en cualquier parlante. Súmales una interfaz de audio y tendrás tu primer home studio.").build(),
                Novedad.builder().titulo("El ukelele: el instrumento ideal para este verano").categoria("Lanzamientos").fecha(LocalDate.of(2026, 11, 17))
                        .autor("Sonido Vivo").imagen("https://images.unsplash.com/photo-1577281747309-25bdad8b7cc0?auto=format&fit=crop&w=800&q=80")
                        .resumen("Liviano, fácil de aprender y perfecto para la playa: llegó el ukelele concierto Kala KA-C.")
                        .descripcionCompleta("Con solo cuatro cuerdas de nailon y acordes sencillos, el ukelele es uno de los instrumentos más fáciles para empezar a tocar en pocos días. El Kala KA-C es de tamaño concierto, un poco más grande que el soprano, lo que da más espacio en el diapasón y un sonido más lleno. Llévalo a tus vacaciones y pregúntanos por nuestras clases").build()
            ));
        }
    }
}
