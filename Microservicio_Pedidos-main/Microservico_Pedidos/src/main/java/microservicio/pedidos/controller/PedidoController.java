package microservicio.pedidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import microservicio.pedidos.model.Pedido;
import microservicio.pedidos.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos API", description = "Operaciones relacionadas a pe")
public class PedidoController {
    
    @Autowired
    private PedidoService pedidoService;
    
    @PostMapping
    @Operation(summary = "Crea una nuevo pedido")
    public ResponseEntity<Pedido> crearPedido(@RequestBody Pedido pedido) {
        return ResponseEntity.ok(pedidoService.crearPedido(pedido));
    }
    
    @PutMapping("/{id}/estado")
    @Operation(summary = "Cambia el estado")
    public ResponseEntity<Pedido> actualizarEstado(
            @PathVariable Long id, 
            @RequestParam String estado) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, estado));
    }
    
    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Ver pedido por la ID del cliente")
    public ResponseEntity<List<Pedido>> obtenerPedidosPorCliente(
            @PathVariable Long clienteId) {
        return ResponseEntity.ok(pedidoService.obtenerPedidosPorCliente(clienteId));
    }
    
    @GetMapping("/sucursal/{sucursalId}")
    @Operation(summary = "Ver pedidos por sucursal")
    public ResponseEntity<List<Pedido>> obtenerPedidosPorSucursal(
            @PathVariable Long sucursalId) {
        return ResponseEntity.ok(pedidoService.obtenerPedidosPorSucursal(sucursalId));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Ver pedido por ID")
    public ResponseEntity<Pedido> obtenerPedidoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPedidoPorId(id));
    }
}