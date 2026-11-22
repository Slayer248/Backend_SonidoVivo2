package cl.sonidovivo.usuario.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

/**
 * Envía la contraseña temporal a las cuentas nuevas. Las credenciales del correo NO están en el código:
 * salen de correo.local.properties (ignorado por git) o de las variables MAIL_USERNAME y MAIL_PASSWORD.
 * Si no hay credenciales, el servicio sigue funcionando en modo desarrollo y muestra la contraseña en la consola.
 */
@Service
@Slf4j
public class CorreoService {

    private final JavaMailSender mailSender;
    private final String remitente;

    public CorreoService(JavaMailSender mailSender, @Value("${spring.mail.username:}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente == null ? "" : remitente.trim();
    }

    public boolean habilitado() {
        return !remitente.isEmpty();
    }

    /** Lanza 503 si el correo está configurado pero no se pudo enviar (así la cuenta no queda creada sin contraseña). */
    public void enviarContrasenaTemporal(String nombre, String correo, String contrasena) {
        if (!habilitado()) {
            log.warn("Correo no configurado (modo desarrollo). Contraseña temporal de {}: {}", correo, contrasena);
            return;
        }
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(mensaje, "UTF-8");
            h.setFrom(new InternetAddress(remitente, "Sonido Vivo"));
            h.setTo(correo);
            h.setSubject("Tu cuenta en Sonido Vivo");
            h.setText(cuerpo(nombre, contrasena), true);
            mailSender.send(mensaje);
            log.info("Contraseña temporal enviada a {}", correo);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo a {}: {}", correo, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo enviar el correo con la contraseña. Revisa que el correo sea correcto e inténtalo de nuevo.");
        }
    }

    private static String cuerpo(String nombre, String contrasena) {
        return "<div style=\"font-family:Arial,sans-serif;max-width:520px;margin:auto;color:#222\">"
                + "<h2 style=\"color:#b8860b\">Sonido Vivo</h2>"
                + "<p>Hola " + HtmlUtils.htmlEscape(nombre) + ", ya tienes una cuenta en nuestra tienda.</p>"
                + "<p>Esta es tu contraseña temporal:</p>"
                + "<p style=\"font-size:22px;font-weight:bold;letter-spacing:2px;background:#f4f4f4;padding:12px 16px;"
                + "border-radius:6px;display:inline-block\">" + HtmlUtils.htmlEscape(contrasena) + "</p>"
                + "<p>Al iniciar sesión por primera vez te pediremos que la cambies por una propia.</p>"
                + "<p style=\"color:#777;font-size:12px\">Si no creaste esta cuenta, ignora este mensaje.</p></div>";
    }
}
