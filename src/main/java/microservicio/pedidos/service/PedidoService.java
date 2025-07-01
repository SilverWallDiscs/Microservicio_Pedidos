package microservicio.pedidos.service;

import microservicio.pedidos.model.Pedido;
import microservicio.pedidos.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private RestTemplate restTemplate;

    public Pedido crearPedido(Pedido pedido) {
        if (pedido.getClienteId() == null || pedido.getSucursalId() == null || pedido.getItems() == null || pedido.getItems().isEmpty()) {
            throw new RuntimeException("Los datos básicos del pedido (clienteId, sucursalId, items) son obligatorios.");
        }

        // if (!verificarDisponibilidad(pedido)) {
        //     throw new RuntimeException("No hay suficiente stock para uno o más productos en el pedido.");
        // }
        
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado("PENDIENTE");
        calcularTotal(pedido);
        return pedidoRepository.save(pedido);
    }

    public Pedido actualizarPedidoCompleto(Long pedidoId, Pedido pedidoDetalles) {
        Pedido pedidoExistente = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + pedidoId));

        if (pedidoDetalles.getClienteId() == null || pedidoDetalles.getSucursalId() == null || pedidoDetalles.getItems() == null || pedidoDetalles.getItems().isEmpty()) {
            throw new RuntimeException("Los datos básicos del pedido (clienteId, sucursalId, items) son obligatorios para la actualización.");
        }

        pedidoExistente.setClienteId(pedidoDetalles.getClienteId());
        pedidoExistente.setSucursalId(pedidoDetalles.getSucursalId());
        pedidoExistente.setFechaEntregaEstimada(pedidoDetalles.getFechaEntregaEstimada());
        pedidoExistente.setEstado(pedidoDetalles.getEstado());
        pedidoExistente.setDireccionEnvio(pedidoDetalles.getDireccionEnvio());
        pedidoExistente.setMetodoPago(pedidoDetalles.getMetodoPago());

        pedidoExistente.getItems().clear();
        if (pedidoDetalles.getItems() != null) {
            pedidoDetalles.getItems().forEach(item -> {
                if (item.getProductoId() == null || item.getCantidad() <= 0 || item.getPrecioUnitario() < 0) {
                    throw new RuntimeException("Los detalles del item (productoId, cantidad, precioUnitario) son obligatorios y deben ser válidos.");
                }
                pedidoExistente.getItems().add(item);
            });
        }
        
        // if (!verificarDisponibilidad(pedidoExistente)) {
        //     throw new RuntimeException("No hay suficiente stock para uno o más productos actualizados en el pedido.");
        // }

        calcularTotal(pedidoExistente);

        return pedidoRepository.save(pedidoExistente);
    }


    public Pedido actualizarEstado(Long pedidoId, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + pedidoId));

        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            throw new RuntimeException("El nuevo estado no puede ser nulo o vacío.");
        }

        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    public List<Pedido> obtenerPedidosPorCliente(Long clienteId) {
        if (clienteId == null || clienteId <= 0) {
            throw new RuntimeException("El ID del cliente no puede ser nulo o inválido.");
        }
        return pedidoRepository.findByClienteId(clienteId);
    }

    public List<Pedido> obtenerPedidosPorSucursal(Long sucursalId) {
        if (sucursalId == null || sucursalId <= 0) {
            throw new RuntimeException("El ID de la sucursal no puede ser nulo o inválido.");
        }
        return pedidoRepository.findBySucursalId(sucursalId);
    }

    public Pedido obtenerPedidoPorId(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("El ID del pedido no puede ser nulo o inválido.");
        }
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    public void deletePedidoById(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("El ID del pedido a eliminar no puede ser nulo o inválido.");
        }
        if (!pedidoRepository.existsById(id)) {
            throw new RuntimeException("Pedido no encontrado con ID: " + id);
        }
        pedidoRepository.deleteById(id);
    }

    private void calcularTotal(Pedido pedido) {
        if (pedido.getItems() == null || pedido.getItems().isEmpty()) {
            pedido.setTotal(0.0);
            return;
        }
        double total = pedido.getItems().stream()
                .mapToDouble(item -> {
                    if (item.getCantidad() <= 0 || item.getPrecioUnitario() < 0) {
                        throw new RuntimeException("La cantidad y precio unitario de los items deben ser valores positivos.");
                    }
                    item.setSubtotal(item.getPrecioUnitario() * item.getCantidad());
                    return item.getSubtotal();
                })
                .sum();
        pedido.setTotal(total);
    }

    // public boolean verificarDisponibilidad(Pedido pedido) {
    //     if (pedido.getItems() == null || pedido.getItems().isEmpty()) {
    //         return true;
    //     }
    //     try {
    //         String inventarioUrl = "http://inventario-service/inventario/verificar-disponibilidad";
    //         return restTemplate.postForObject(inventarioUrl, pedido.getItems(), Boolean.class);
    //     } catch (Exception e) {
    //         System.err.println("Error al verificar disponibilidad con el servicio de inventario: " + e.getMessage());
    //         return false;
    //     }
    }