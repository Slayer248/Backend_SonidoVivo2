package cl.sonidovivo.producto.config;

import cl.sonidovivo.producto.model.Producto;
import cl.sonidovivo.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/** Catálogo inicial tomado de src/data/productos.ts del front. */
@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    @Override
    public void run(String... args) {
        if (productoRepository.count() > 0) return;
        Object[][] datos = {
                {"Guitarra Eléctrica Fender Stratocaster", "Instrumentos de cuerda", 750000, 5, "https://images.unsplash.com/photo-1550291652-6ea9114a47b1?auto=format&fit=crop&w=400&q=80"},
                {"Teclado Roland XPS-10", "Teclados", 620000, 2, "https://images.unsplash.com/photo-1520523839897-bd0b52f945a0?auto=format&fit=crop&w=400&q=80"},
                {"Batería Acústica Pearl Roadshow", "Baterías", 580000, 0, "https://images.unsplash.com/photo-1519892300165-cb5542fb47c7?auto=format&fit=crop&w=400&q=80"},
                {"Pedal de Efectos Boss DS-1", "Accesorios", 85000, 12, "https://images.unsplash.com/photo-1564186763535-ebb21ef5277f?auto=format&fit=crop&w=400&q=80"},
                {"Guitarra Acústica Yamaha F310", "Instrumentos de cuerda", 159990, 8, "https://images.unsplash.com/photo-1588690224648-20426ab6274a?auto=format&fit=crop&w=400&q=80"},
                {"Bajo Eléctrico Ibanez GSR200", "Instrumentos de cuerda", 289990, 4, "https://images.unsplash.com/photo-1727035305536-e027445c24e3?auto=format&fit=crop&w=400&q=80"},
                {"Teclado Casio CT-S300", "Teclados", 149990, 6, "https://images.unsplash.com/photo-1534170106611-6b85b35795f5?auto=format&fit=crop&w=400&q=80"},
                {"Piano Digital Yamaha P-145", "Teclados", 549990, 3, "https://images.unsplash.com/photo-1567787609897-efa3625dd22d?auto=format&fit=crop&w=400&q=80"},
                {"Batería Electrónica Alesis Nitro Max", "Baterías", 449990, 2, "https://images.unsplash.com/photo-1457523054379-8d03ab9fc2aa?auto=format&fit=crop&w=400&q=80"},
                {"Amplificador Marshall MG15G", "Audio", 129990, 7, "https://images.unsplash.com/photo-1534797993987-4a363509bc52?auto=format&fit=crop&w=400&q=80"},
                {"Micrófono Shure SM58", "Audio", 119990, 10, "https://images.unsplash.com/photo-1543062591-e3c0fdb97350?auto=format&fit=crop&w=400&q=80"},
                {"Cuerdas Ernie Ball Regular Slinky", "Accesorios", 9990, 40, "https://images.unsplash.com/photo-1681059840890-230a07b85448?auto=format&fit=crop&w=400&q=80"},
                {"Ukelele Concierto Kala KA-C", "Instrumentos de cuerda", 79990, 9, "https://images.unsplash.com/photo-1577281747309-25bdad8b7cc0?auto=format&fit=crop&w=400&q=80"},
                {"Audífonos de Estudio Audio-Technica ATH-M50x", "Audio", 169990, 5, "https://images.unsplash.com/photo-1737885197946-6d9d79dade89?auto=format&fit=crop&w=400&q=80"},
                {"Baquetas Vic Firth 5A", "Accesorios", 14990, 30, "https://images.unsplash.com/photo-1602001373676-64e1598dbc6a?auto=format&fit=crop&w=400&q=80"},
                {"Sintetizador Korg Minilogue XD", "Teclados", 699990, 1, "https://images.unsplash.com/photo-1642784323419-89d08b21c4de?auto=format&fit=crop&w=400&q=80"}
        };
        Arrays.stream(datos).forEach(d -> productoRepository.save(Producto.builder()
                .nombre((String) d[0]).categoria((String) d[1])
                .precio((Integer) d[2]).stock((Integer) d[3]).img((String) d[4])
                .build()));
    }
}
