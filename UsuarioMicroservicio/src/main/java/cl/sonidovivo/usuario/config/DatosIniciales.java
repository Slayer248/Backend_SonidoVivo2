package cl.sonidovivo.usuario.config;

import cl.sonidovivo.usuario.client.RolClient;
import cl.sonidovivo.usuario.model.Usuario;
import cl.sonidovivo.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Usuarios de prueba (los del front, más un Vendedor). Necesita RolMicroservicio levantado;
 * si no está disponible se omite y se reintenta en el próximo arranque.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolClient rolClient;

    @Override
    public void run(String... args) {
        try {
            if (usuarioRepository.count() == 0) {
                BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
                Long admin = rolClient.obtenerPorNombre("Administrador").id();
                Long vendedor = rolClient.obtenerPorNombre("Vendedor").id();
                Long cliente = rolClient.obtenerPorNombre("Cliente").id();
                usuarioRepository.save(Usuario.builder().run("11111111-1").nombre("Admin").apellidos("Sonido Vivo")
                        .correo("admin@sonidovivo.cl").contrasena(enc.encode("admin123")).rolId(admin).build());
                usuarioRepository.save(Usuario.builder().run("13579246-2").nombre("Valentina").apellidos("Rojas")
                        .correo("vendedor@sonidovivo.cl").contrasena(enc.encode("vendedor123")).rolId(vendedor).build());
                usuarioRepository.save(Usuario.builder().run("22222222-2").nombre("Juan").apellidos("Pérez")
                        .correo("cliente@sonidovivo.cl").contrasena(enc.encode("user123")).rolId(cliente).build());
            }
            // Las cuentas de prueba llevan dirección para que el pago se autocomplete.
            // Solo se completan si están vacías: nunca pisa lo que alguien haya editado.
            completarDireccion("admin@sonidovivo.cl", "Av. Libertad 450", "Viña del Mar", "Valparaíso");
            completarDireccion("vendedor@sonidovivo.cl", "Calle Valparaíso 680", "Viña del Mar", "Valparaíso");
            completarDireccion("cliente@sonidovivo.cl", "Los Aromos 1234", "Quilpué", "Valparaíso");
        } catch (Exception e) {
            log.warn("No se cargaron los usuarios iniciales (¿RolMicroservicio está levantado?): {}", e.getMessage());
        }
    }

    private void completarDireccion(String correo, String direccion, String comuna, String region) {
        usuarioRepository.findByCorreoIgnoreCase(correo).ifPresent(u -> {
            boolean vacia = (u.getDireccion() == null || u.getDireccion().isBlank())
                    && (u.getComuna() == null || u.getComuna().isBlank())
                    && (u.getRegion() == null || u.getRegion().isBlank());
            if (vacia) {
                u.setDireccion(direccion);
                u.setComuna(comuna);
                u.setRegion(region);
                usuarioRepository.save(u);
            }
        });
    }
}
