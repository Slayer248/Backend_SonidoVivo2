package cl.sonidovivo.rol.config;

import cl.sonidovivo.rol.model.Rol;
import cl.sonidovivo.rol.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Roles del front: Administrador, Vendedor y Cliente. */
@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final RolRepository rolRepository;

    @Override
    public void run(String... args) {
        if (rolRepository.count() == 0) {
            rolRepository.saveAll(List.of(
                    Rol.builder().nombre("Administrador").build(),
                    Rol.builder().nombre("Vendedor").build(),
                    Rol.builder().nombre("Cliente").build()
            ));
        }
    }
}
