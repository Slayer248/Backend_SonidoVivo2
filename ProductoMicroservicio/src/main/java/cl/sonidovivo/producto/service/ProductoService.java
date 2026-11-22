package cl.sonidovivo.producto.service;

import cl.sonidovivo.producto.dto.ProductoRequest;
import cl.sonidovivo.producto.model.Producto;
import cl.sonidovivo.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    public List<Producto> listar(String buscar, String categoria) {
        String texto = buscar == null ? "" : buscar.trim().toLowerCase();
        String cat = categoria == null ? "" : categoria.trim();
        return productoRepository.findAllByOrderByIdAsc().stream()
                .filter(p -> texto.isEmpty() || p.getNombre().toLowerCase().contains(texto))
                .filter(p -> cat.isEmpty() || cat.equalsIgnoreCase("Todas") || p.getCategoria().equalsIgnoreCase(cat))
                .toList();
    }

    public List<String> categorias() {
        return productoRepository.findAll().stream()
                .map(Producto::getCategoria).distinct().sorted().toList();
    }

    public Producto obtener(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    public Producto crear(ProductoRequest d) {
        validar(d);
        return productoRepository.save(Producto.builder()
                .nombre(d.nombre().trim())
                .categoria(d.categoria().trim())
                .precio(d.precio())
                .stock(d.stock())
                .img(d.img() == null ? null : d.img().trim())
                .build());
    }

    public Producto actualizar(Long id, ProductoRequest d) {
        Producto p = obtener(id);
        validar(d);
        p.setNombre(d.nombre().trim());
        p.setCategoria(d.categoria().trim());
        p.setPrecio(d.precio());
        p.setStock(d.stock());
        p.setImg(d.img() == null ? null : d.img().trim());
        return productoRepository.save(p);
    }

    public void eliminar(Long id) {
        productoRepository.delete(obtener(id));
    }

    @Transactional
    public Producto descontarStock(Long id, Integer cantidad) {
        validarCantidad(cantidad);
        obtener(id);
        if (productoRepository.descontarStock(id, cantidad) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para el producto " + id);
        }
        return productoRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Producto reponerStock(Long id, Integer cantidad) {
        validarCantidad(cantidad);
        obtener(id);
        productoRepository.reponerStock(id, cantidad);
        return productoRepository.findById(id).orElseThrow();
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor a 0");
        }
    }

    private void validar(ProductoRequest d) {
        if (d.nombre() == null || d.nombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        if (d.categoria() == null || d.categoria().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoría es obligatoria");
        }
        if (d.precio() == null || d.precio() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio debe ser mayor a 0");
        }
        if (d.stock() == null || d.stock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El stock no puede ser negativo");
        }
    }
}
