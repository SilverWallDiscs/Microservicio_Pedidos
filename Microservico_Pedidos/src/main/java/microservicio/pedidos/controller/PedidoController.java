package microservicio.pedidos.controller;

import microservicio.pedidos.model.Pedido;
import microservicio.pedidos.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    
    @Autowired
    private PedidoService pedidoService;
    
    @PostMapping
    public ResponseEntity<Pedido> crearPedido(@RequestBody Pedido pedido) {
        return ResponseEntity.ok(pedidoService.crearPedido(pedido));
    }
    
    @PutMapping("/{id}/estado")
    public ResponseEntity<Pedido> actualizarEstado(
            @PathVariable Long id, 
            @RequestParam String estado) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, estado));
    }
    
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Pedido>> obtenerPedidosPorCliente(
            @PathVariable Long clienteId) {
        return ResponseEntity.ok(pedidoService.obtenerPedidosPorCliente(clienteId));
    }
    
    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<List<Pedido>> obtenerPedidosPorSucursal(
            @PathVariable Long sucursalId) {
        return ResponseEntity.ok(pedidoService.obtenerPedidosPorSucursal(sucursalId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obtenerPedidoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPedidoPorId(id));
    }
}