package microservicio.pedidos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import microservicio.pedidos.model.Pedido;
import microservicio.pedidos.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos API")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "Crea una nueva orden")
    public ResponseEntity<Pedido> crearPedido(@Valid @RequestBody Pedido pedido) {
        try {
            Pedido nuevoPedido = pedidoService.crearPedido(pedido);
            return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una orden completamente")
    public ResponseEntity<Pedido> actualizarPedidoCompleto(@PathVariable Long id, @Valid @RequestBody Pedido pedido) {
        try {
            Pedido pedidoActualizado = pedidoService.actualizarPedidoCompleto(id, pedido);
            return new ResponseEntity<>(pedidoActualizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Actualiza el estado de una orden")
    public ResponseEntity<Pedido> actualizarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {
        try {
            Pedido pedidoActualizado = pedidoService.actualizarEstado(id, estado);
            return new ResponseEntity<>(pedidoActualizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Busca orden por ID de cliente")
    public ResponseEntity<List<Pedido>> obtenerPedidosPorCliente(
            @PathVariable Long clienteId) {
        List<Pedido> pedidos = pedidoService.obtenerPedidosPorCliente(clienteId);
        if (pedidos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(pedidos, HttpStatus.OK);
    }

    @GetMapping("/sucursal/{sucursalId}")
    @Operation(summary = "Busca orden por ID de sucursal")
    public ResponseEntity<List<Pedido>> obtenerPedidosPorSucursal(
            @PathVariable Long sucursalId) {
        List<Pedido> pedidos = pedidoService.obtenerPedidosPorSucursal(sucursalId);
        if (pedidos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(pedidos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca orden por ID")
    public ResponseEntity<Pedido> obtenerPedidoPorId(@PathVariable Long id) {
        try {
            Pedido pedido = pedidoService.obtenerPedidoPorId(id);
            return new ResponseEntity<>(pedido, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina orden por ID")
    public ResponseEntity<Void> eliminarPedidoPorId(@PathVariable Long id) {
        try {
            pedidoService.deletePedidoById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}