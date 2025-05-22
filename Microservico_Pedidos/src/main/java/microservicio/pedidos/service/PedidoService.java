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
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado("PENDIENTE");
        calcularTotal(pedido);
        return pedidoRepository.save(pedido);
    }
    
    public Pedido actualizarEstado(Long pedidoId, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }
    
    public List<Pedido> obtenerPedidosPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }
    
    public List<Pedido> obtenerPedidosPorSucursal(Long sucursalId) {
        return pedidoRepository.findBySucursalId(sucursalId);
    }
    
    public Pedido obtenerPedidoPorId(Long id) {
        return pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }
    
    private void calcularTotal(Pedido pedido) {
        double total = pedido.getItems().stream()
            .mapToDouble(item -> {
                item.setSubtotal(item.getPrecioUnitario() * item.getCantidad());
                return item.getSubtotal();
            })
            .sum();
        pedido.setTotal(total);
    }
    
    public boolean verificarDisponibilidad(Pedido pedido) {
        String inventarioUrl = "http://inventario-service/inventario/verificar-disponibilidad";
        return restTemplate.postForObject(inventarioUrl, pedido.getItems(), Boolean.class);
    }
}