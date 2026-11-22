package cl.sonidovivo.venta.service;

import cl.sonidovivo.venta.dto.LineaBoleta;
import cl.sonidovivo.venta.model.Venta;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Envía la boleta al correo del cliente después de un pago aprobado. Se hace en segundo plano y nunca hace fallar
 * la compra: si el correo no está configurado o falla, solo queda un aviso en la consola.
 * Las credenciales salen de correo.local.properties (ignorado por git) o de MAIL_USERNAME / MAIL_PASSWORD.
 */
@Service
@Slf4j
public class BoletaCorreoService {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final NumberFormat PESOS = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CL"));
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final JavaMailSender mailSender;
    private final String remitente;

    public BoletaCorreoService(JavaMailSender mailSender, @Value("${spring.mail.username:}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente == null ? "" : remitente.trim();
    }

    /** Programa el envío y vuelve de inmediato. */
    public void enviarEnSegundoPlano(Venta venta, List<LineaBoleta> lineas, String direccion) {
        if (venta.getEmail() == null || !EMAIL.matcher(venta.getEmail().trim()).matches()) {
            log.info("Boleta {} sin correo válido: no se envió", venta.getNumeroBoleta());
            return;
        }
        if (remitente.isEmpty()) {
            log.warn("Correo no configurado: la boleta {} no se envió a {}", venta.getNumeroBoleta(), venta.getEmail());
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                MimeMessage mensaje = mailSender.createMimeMessage();
                MimeMessageHelper h = new MimeMessageHelper(mensaje, "UTF-8");
                h.setFrom(new InternetAddress(remitente, "Sonido Vivo"));
                h.setTo(venta.getEmail().trim());
                h.setSubject("Tu boleta " + venta.getNumeroBoleta() + " - Sonido Vivo");
                h.setText(html(venta, lineas, direccion), true);
                mailSender.send(mensaje);
                log.info("Boleta {} enviada a {}", venta.getNumeroBoleta(), venta.getEmail());
            } catch (Exception e) {
                log.error("No se pudo enviar la boleta {} a {}: {}", venta.getNumeroBoleta(), venta.getEmail(), e.getMessage());
            }
        });
    }

    static String html(Venta v, List<LineaBoleta> lineas, String direccion) {
        StringBuilder filas = new StringBuilder();
        for (LineaBoleta l : lineas) {
            filas.append("<tr><td style=\"padding:6px 8px;border-bottom:1px solid #eee\">").append(esc(l.nombre()))
                    .append("</td><td style=\"padding:6px 8px;border-bottom:1px solid #eee;text-align:center\">").append(l.cantidad())
                    .append("</td><td style=\"padding:6px 8px;border-bottom:1px solid #eee;text-align:right\">").append(pesos(l.precioUnitario()))
                    .append("</td><td style=\"padding:6px 8px;border-bottom:1px solid #eee;text-align:right\">")
                    .append(pesos(l.precioUnitario() * l.cantidad())).append("</td></tr>");
        }
        return "<div style=\"font-family:Arial,sans-serif;max-width:600px;margin:auto;color:#222\">"
                + "<h2 style=\"margin-bottom:0\">Sonido Vivo SpA</h2>"
                + "<div style=\"color:#777;font-size:12px\">RUT 76.123.456-7 · Av. Libertad 450, Viña del Mar</div>"
                + "<p style=\"border:2px solid #c0392b;color:#c0392b;display:inline-block;padding:6px 14px;font-weight:bold\">"
                + "BOLETA ELECTRÓNICA N° " + esc(v.getNumeroBoleta()) + "</p>"
                + "<p>¡Gracias por tu compra, " + esc(v.getClienteNombre()) + "!</p>"
                + "<p style=\"font-size:13px\">Fecha: " + v.getFecha().format(FECHA)
                + (v.getPedidoCodigo() != null ? "<br>Pedido: " + esc(v.getPedidoCodigo()) : "")
                + (v.getRut() != null && !v.getRut().isBlank() ? "<br>RUT: " + esc(v.getRut()) : "")
                + (direccion != null && !direccion.isBlank() ? "<br>Despacho: " + esc(direccion) : "")
                + (v.getTarjetaUltimos4() != null ? "<br>Pago con tarjeta terminada en " + esc(v.getTarjetaUltimos4()) : "")
                + "</p>"
                + "<table style=\"width:100%;border-collapse:collapse;font-size:14px\">"
                + "<tr style=\"background:#f4f4f4\"><th style=\"padding:6px 8px;text-align:left\">Producto</th>"
                + "<th style=\"padding:6px 8px\">Cant.</th><th style=\"padding:6px 8px;text-align:right\">Precio unit.</th>"
                + "<th style=\"padding:6px 8px;text-align:right\">Subtotal</th></tr>" + filas + "</table>"
                + "<table style=\"margin-left:auto;margin-top:12px;font-size:14px\">"
                + "<tr><td style=\"padding:2px 12px\">Neto</td><td style=\"text-align:right\">" + pesos(v.getNeto()) + "</td></tr>"
                + "<tr><td style=\"padding:2px 12px\">IVA (19%)</td><td style=\"text-align:right\">" + pesos(v.getIva()) + "</td></tr>"
                + "<tr><td style=\"padding:6px 12px;font-weight:bold;font-size:16px;border-top:1px solid #ccc\">Total</td>"
                + "<td style=\"text-align:right;font-weight:bold;font-size:16px;border-top:1px solid #ccc\">" + pesos(v.getTotal()) + "</td></tr></table>"
                + "<p style=\"color:#777;font-size:12px;margin-top:24px\">Boleta de una compra simulada. Puedes ver tus pedidos en \"Mis pedidos\" de la tienda.</p></div>";
    }

    private static String pesos(int monto) {
        return "$" + PESOS.format(monto);
    }

    private static String esc(String s) {
        return s == null ? "" : HtmlUtils.htmlEscape(s);
    }
}
