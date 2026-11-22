package cl.sonidovivo.pedido.service;

import cl.sonidovivo.pedido.client.ProductoClient;
import cl.sonidovivo.pedido.client.PuntoRetiroClient;
import cl.sonidovivo.pedido.dto.ItemRequest;
import cl.sonidovivo.pedido.dto.PedidoRequest;
import cl.sonidovivo.pedido.dto.ProductoDto;
import cl.sonidovivo.pedido.model.EstadoPedido;
import cl.sonidovivo.pedido.model.ItemPedido;
import cl.sonidovivo.pedido.model.Pedido;
import cl.sonidovivo.pedido.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final double TASA_IVA = 0.19;
    private static final int BASE_CODIGO = 100;

    private final PedidoRepository pedidoRepository;
    private final ProductoClient productoClient;
    private final PuntoRetiroClient puntoRetiroClient;

    public List<Pedido> listar(String estado, String email) {
        if (estado != null && !estado.isBlank()) {
            return pedidoRepository.findByEstadoOrderByIdDesc(parsearEstado(estado));
        }
        if (email != null && !email.isBlank()) {
            return pedidoRepository.findByEmailIgnoreCaseOrderByIdDesc(email.trim());
        }
        return pedidoRepository.findAllByOrderByIdDesc();
    }

    /** Pedidos de una cuenta, más recientes primero. */
    public List<Pedido> listarDeUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioIdOrderByIdDesc(usuarioId);
    }

    /**
     * Obtiene un pedido para quien lo consulta. Un cliente solo puede ver los suyos
     * (si es de otra persona responde 404, para no revelar que existe).
     */
    public Pedido obtenerParaUsuario(String codigo, Long usuarioId, String rol) {
        Pedido p = obtener(codigo);
        boolean esPersonal = "Administrador".equals(rol) || "Vendedor".equals(rol);
        if (usuarioId != null && !esPersonal && !usuarioId.equals(p.getUsuarioId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado");
        }
        return p;
    }

    public Pedido obtener(String codigo) {
        return pedidoRepository.findByCodigo(codigo.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    }

    /**
     * Crea el pedido: valida productos, descuenta stock y calcula la boleta con los precios del servidor.
     * Si algo falla a mitad de camino se repone el stock ya descontado.
     */
    @Transactional
    public Pedido crear(PedidoRequest d) {
        validar(d);
        if (d.puntoRetiroId() != null) {
            puntoRetiroClient.verificarExiste(d.puntoRetiroId());
        }

        List<ItemRequest> descontados = new ArrayList<>();
        List<ItemPedido> items = new ArrayList<>();
        int total = 0;
        try {
            for (ItemRequest it : d.items()) {
                ProductoDto p = productoClient.obtener(it.productoId());
                productoClient.descontarStock(p.id(), it.cantidad());
                descontados.add(it);
                items.add(ItemPedido.builder().productoId(p.id()).nombre(p.nombre())
                        .precio(p.precio()).cantidad(it.cantidad()).img(p.img()).build());
                total += p.precio() * it.cantidad();
            }
            int neto = (int) Math.round(total / (1 + TASA_IVA));

            Pedido pedido = Pedido.builder()
                    .clienteNombre(d.clienteNombre().trim())
                    .rut(d.rut() == null ? null : d.rut().trim())
                    .email(d.email() == null ? null : d.email().trim())
                    .direccion(d.direccion() == null ? null : d.direccion().trim())
                    .puntoRetiroId(d.puntoRetiroId())
                    .usuarioId(d.usuarioId())
                    .fecha(LocalDate.now())
                    .estado(EstadoPedido.EN_PREPARACION)
                    .neto(neto).iva(total - neto).total(total)
                    .items(items)
                    .build();
            Pedido guardado = pedidoRepository.save(pedido);
            guardado.setCodigo("PED-" + (BASE_CODIGO + guardado.getId()));
            return pedidoRepository.save(guardado);
        } catch (RuntimeException e) {
            descontados.forEach(it -> productoClient.reponerStock(it.productoId(), it.cantidad()));
            throw e;
        }
    }

    public Pedido cambiarEstado(String codigo, String estado) {
        Pedido p = obtener(codigo);
        p.setEstado(parsearEstado(estado));
        return pedidoRepository.save(p);
    }

    private EstadoPedido parsearEstado(String estado) {
        try {
            EstadoPedido e = EstadoPedido.desde(estado);
            if (e == null) throw new IllegalArgumentException();
            return e;
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Estado inválido. Valores: En Preparación, Despachado, Entregado");
        }
    }

    private void validar(PedidoRequest d) {
        if (d.clienteNombre() == null || d.clienteNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del cliente es obligatorio");
        }
        if (d.items() == null || d.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido debe tener al menos un producto");
        }
        for (ItemRequest it : d.items()) {
            if (it.productoId() == null || it.cantidad() == null || it.cantidad() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cada producto necesita id y cantidad mayor a 0");
            }
        }
    }
}
