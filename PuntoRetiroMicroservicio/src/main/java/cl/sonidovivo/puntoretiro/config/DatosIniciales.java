package cl.sonidovivo.puntoretiro.config;

import cl.sonidovivo.puntoretiro.model.PuntoRetiro;
import cl.sonidovivo.puntoretiro.repository.PuntoRetiroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Sucursales iniciales tomadas de MapaPage.tsx del front. */
@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final PuntoRetiroRepository puntoRetiroRepository;

    @Override
    public void run(String... args) {
        if (puntoRetiroRepository.count() == 0) {
            puntoRetiroRepository.saveAll(List.of(
                    PuntoRetiro.builder().nombre("Tienda Principal Sonido Vivo").tipo("Tienda")
                            .direccion("Av. Libertad 450, Viña del Mar").horario("10:00 - 19:00 hrs")
                            .lat(-33.0183).lng(-71.5511).principal(true).build(),
                    PuntoRetiro.builder().nombre("Punto de Retiro Centro Viña").tipo("Punto de retiro")
                            .direccion("Calle Valparaíso 680, Viña del Mar").horario("11:00 - 18:00 hrs")
                            .lat(-33.0245).lng(-71.5522).principal(false).build()
            ));
        }
    }
}
