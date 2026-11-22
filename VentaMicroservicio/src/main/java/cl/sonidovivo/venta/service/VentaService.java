package cl.sonidovivo.venta.service;

import cl.sonidovivo.venta.client.PedidoClient;
import cl.sonidovivo.venta.client.ProductoClient;
import cl.sonidovivo.venta.dto.*;
import cl.sonidovivo.venta.model.EstadoPago;
import cl.sonidovivo.venta.model.Venta;
import cl.sonidovivo.venta.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaService {

    private static final double TASA_IVA = 0.19;

    private final VentaRepository ventaRepository;
    private final ProductoClient productoClient;
    private final PedidoClient pedidoClient;
    private final BoletaCorreoService boletaCorreoService;

    /**
     * Flujo de compra del front: valida stock → simula el pago → crea el pedido → emite la boleta.
     * Una tarjeta que termina en 0000 es rechazada (igual que en simularPago() del front);
     * el intento rechazado también queda registrado y responde 402.
     */
    public Venta checkout(CheckoutRequest d, Long usuarioId) {
        validar(d);

        int total = 0;
        List<LineaBoleta> lineas = new ArrayList<>();
        for (ItemRequest it : d.items()) {
            ProductoDto p = productoClient.obtener(it.productoId());
            if (p.stock() < it.cantidad()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para " + p.nombre());
            }
            total += p.precio() * it.cantidad();
            lineas.add(new LineaBoleta(p.nombre(), it.cantidad(), p.precio()));
        }
        int neto = (int) Math.round(total / (1 + TASA_IVA));

        String digitos = d.numeroTarjeta().replaceAll("\\s", "");
        String ultimos4 = digitos.substring(digitos.length() - 4);

        Venta venta = Venta.builder()
                .clienteNombre(d.clienteNombre().trim())
                .rut(d.rut()).email(d.email())
                .neto(neto).iva(total - neto).total(total)
                .tarjetaUltimos4(ultimos4)
                .fecha(LocalDateTime.now())
                .build();

        if (ultimos4.equals("0000")) {
            return rechazar(venta, "Pago rechazado por el banco emisor");
        }

        PedidoDto pedido;
        try {
            pedido = pedidoClient.crear(new PedidoRequestDto(d.clienteNombre(), d.rut(), d.email(),
                    d.direccion(), d.puntoRetiroId(), d.items(), usuarioId));
        } catch (ResponseStatusException e) {
            rechazar(venta, e.getReason());
            throw e;
        }

        venta.setEstadoPago(EstadoPago.APROBADO);
        venta.setPedidoCodigo(pedido.codigo());
        Venta guardada = ventaRepository.save(venta);
        guardada.setNumeroBoleta(String.format("B-%06d", guardada.getId()));
        Venta emitida = ventaRepository.save(guardada);
        // La boleta llega al correo del cliente; si el envío falla, la compra igual queda aprobada
        boletaCorreoService.enviarEnSegundoPlano(emitida, lineas, d.direccion());
        return emitida;
    }

    public List<Venta> listar(String estado) {
        if (estado == null || estado.isBlank()) return ventaRepository.findAllByOrderByIdDesc();
        try {
            return ventaRepository.findByEstadoPagoOrderByIdDesc(EstadoPago.valueOf(estado.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido. Valores: APROBADO, RECHAZADO");
        }
    }

    public Venta obtener(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));
    }

    public Venta obtenerPorBoleta(String numero) {
        return ventaRepository.findByNumeroBoleta(numero.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Boleta no encontrada"));
    }

    public ResumenVentas resumen() {
        List<Venta> todas = ventaRepository.findAll();
        List<Venta> aprobadas = todas.stream().filter(v -> v.getEstadoPago() == EstadoPago.APROBADO).toList();
        return new ResumenVentas(
                aprobadas.size(),
                todas.size() - aprobadas.size(),
                aprobadas.stream().mapToLong(Venta::getNeto).sum(),
                aprobadas.stream().mapToLong(Venta::getIva).sum(),
                aprobadas.stream().mapToLong(Venta::getTotal).sum());
    }

    private Venta rechazar(Venta venta, String motivo) {
        venta.setEstadoPago(EstadoPago.RECHAZADO);
        venta.setMotivo(motivo != null && motivo.length() > 250 ? motivo.substring(0, 250) : motivo);
        ventaRepository.save(venta);
        throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, motivo);
    }

    private void validar(CheckoutRequest d) {
        if (d.clienteNombre() == null || d.clienteNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del cliente es obligatorio");
        }
        if (d.items() == null || d.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El carrito está vacío");
        }
        for (ItemRequest it : d.items()) {
            if (it.productoId() == null || it.cantidad() == null || it.cantidad() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cada producto necesita id y cantidad mayor a 0");
            }
        }
        if (d.numeroTarjeta() == null || !d.numeroTarjeta().replaceAll("\\s", "").matches("\\d{16}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La tarjeta debe tener 16 dígitos");
        }
    }
}
