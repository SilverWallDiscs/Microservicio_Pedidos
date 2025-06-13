package microservicio.comentarios;

import microservicio.pedidos.model.ItemPedido;
import microservicio.pedidos.model.Pedido;
import microservicio.pedidos.service.PedidoService;
import microservicio.pedidos.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido testPedido;
    private ItemPedido testItem;

    @BeforeEach
    void setUp() {
        testItem = new ItemPedido();
        testItem.setItemId(1L);
        testItem.setProductoId(101L);
        testItem.setCantidad(2);
        testItem.setPrecioUnitario(10.0);
        testItem.setSubtotal(20.0);

        testPedido = new Pedido();
        testPedido.setPedidoId(1L);
        testPedido.setClienteId(10L);
        testPedido.setSucursalId(1L);
        testPedido.setFechaPedido(null);
        testPedido.setEstado(null);
        testPedido.setTotal(0.0);
        testPedido.setDireccionEnvio("Calle Falsa 123");
        testPedido.setMetodoPago("Tarjeta");
        testPedido.setItems(Arrays.asList(testItem));
    }

    @Test
    void whenCrearPedido_thenReturnPedidoCreatedAndCalculatedTotal() {
        when(restTemplate.postForObject(anyString(), any(List.class), eq(Boolean.class)))
                .thenReturn(true);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido createdPedido = pedidoService.crearPedido(testPedido);

        assertNotNull(createdPedido);
        assertEquals("PENDIENTE", createdPedido.getEstado());
        assertNotNull(createdPedido.getFechaPedido());
        assertEquals(20.0, createdPedido.getTotal());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(restTemplate, times(1)).postForObject(eq("http://inventario-service/inventario/verificar-disponibilidad"), anyList(), eq(Boolean.class));
    }

    @Test
    void whenCrearPedido_withNoStock_thenThrowRuntimeException() {
        when(restTemplate.postForObject(anyString(), any(List.class), eq(Boolean.class)))
                .thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.crearPedido(testPedido);
        });

        assertEquals("No hay suficiente stock para uno o más productos en el pedido.", exception.getMessage());
        verify(pedidoRepository, times(0)).save(any(Pedido.class));
        verify(restTemplate, times(1)).postForObject(eq("http://inventario-service/inventario/verificar-disponibilidad"), anyList(), eq(Boolean.class));
    }

    @Test
    void whenCrearPedido_withMissingRequiredData_thenThrowRuntimeException() {
        testPedido.setClienteId(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.crearPedido(testPedido);
        });
        assertEquals("Los datos básicos del pedido (clienteId, sucursalId, items) son obligatorios.", exception.getMessage());
        verify(pedidoRepository, times(0)).save(any(Pedido.class));
        verify(restTemplate, times(0)).postForObject(anyString(), anyList(), eq(Boolean.class));
    }

    @Test
    void whenActualizarPedidoCompleto_thenReturnUpdatedPedido() {
        Pedido existingPedido = new Pedido();
        existingPedido.setPedidoId(1L);
        existingPedido.setClienteId(10L);
        existingPedido.setSucursalId(1L);
        existingPedido.setEstado("PENDIENTE");
        existingPedido.setTotal(20.0);
        existingPedido.setFechaPedido(LocalDateTime.now().minusDays(1));
        existingPedido.setItems(Arrays.asList(testItem));

        Pedido updatedDetails = new Pedido();
        updatedDetails.setClienteId(11L);
        updatedDetails.setSucursalId(2L);
        updatedDetails.setEstado("EN_CAMINO");
        updatedDetails.setDireccionEnvio("Nueva Direccion 456");
        updatedDetails.setMetodoPago("PayPal");
        updatedDetails.setFechaEntregaEstimada(LocalDateTime.now().plusDays(2));

        ItemPedido newItem = new ItemPedido();
        newItem.setProductoId(202L);
        newItem.setCantidad(1);
        newItem.setPrecioUnitario(50.0);
        updatedDetails.setItems(Arrays.asList(newItem));

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(existingPedido));
        when(restTemplate.postForObject(anyString(), any(List.class), eq(Boolean.class)))
                .thenReturn(true);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido result = pedidoService.actualizarPedidoCompleto(1L, updatedDetails);

        assertNotNull(result);
        assertEquals(1L, result.getPedidoId());
        assertEquals(11L, result.getClienteId());
        assertEquals(2L, result.getSucursalId());
        assertEquals("EN_CAMINO", result.getEstado());
        assertEquals("Nueva Direccion 456", result.getDireccionEnvio());
        assertEquals("PayPal", result.getMetodoPago());
        assertNotNull(result.getFechaEntregaEstimada());
        assertEquals(50.0, result.getTotal());
        assertEquals(1, result.getItems().size());
        assertEquals(newItem.getProductoId(), result.getItems().get(0).getProductoId());
        verify(pedidoRepository, times(1)).save(existingPedido);
        verify(restTemplate, times(1)).postForObject(eq("http://inventario-service/inventario/verificar-disponibilidad"), anyList(), eq(Boolean.class));
    }

    @Test
    void whenActualizarPedidoCompleto_withNotFoundId_thenThrowRuntimeException() {
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.actualizarPedidoCompleto(99L, testPedido);
        });
        assertEquals("Pedido no encontrado con ID: 99", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void whenActualizarPedidoCompleto_withNoStockForUpdatedItems_thenThrowRuntimeException() {
        Pedido existingPedido = new Pedido();
        existingPedido.setPedidoId(1L);
        existingPedido.setClienteId(10L);
        existingPedido.setSucursalId(1L);
        existingPedido.setItems(Arrays.asList(testItem));

        Pedido updatedDetails = new Pedido();
        updatedDetails.setClienteId(11L);
        updatedDetails.setSucursalId(2L);
        ItemPedido newItem = new ItemPedido();
        newItem.setProductoId(202L);
        newItem.setCantidad(1000);
        newItem.setPrecioUnitario(1.0);
        updatedDetails.setItems(Arrays.asList(newItem));

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(existingPedido));
        when(restTemplate.postForObject(anyString(), any(List.class), eq(Boolean.class)))
                .thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.actualizarPedidoCompleto(1L, updatedDetails);
        });
        assertEquals("No hay suficiente stock para uno o más productos actualizados en el pedido.", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(restTemplate, times(1)).postForObject(eq("http://inventario-service/inventario/verificar-disponibilidad"), anyList(), eq(Boolean.class));
    }

    @Test
    void whenActualizarPedidoCompleto_withInvalidItemDetails_thenThrowRuntimeException() {
        Pedido existingPedido = new Pedido();
        existingPedido.setPedidoId(1L);
        existingPedido.setClienteId(10L);
        existingPedido.setSucursalId(1L);
        existingPedido.setItems(Arrays.asList(testItem));

        Pedido updatedDetails = new Pedido();
        updatedDetails.setClienteId(11L);
        updatedDetails.setSucursalId(2L);
        ItemPedido invalidItem = new ItemPedido();
        invalidItem.setProductoId(null);
        invalidItem.setCantidad(5);
        invalidItem.setPrecioUnitario(10.0);
        updatedDetails.setItems(Arrays.asList(invalidItem));

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(existingPedido));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.actualizarPedidoCompleto(1L, updatedDetails);
        });
        assertEquals("Los detalles del item (productoId, cantidad, precioUnitario) son obligatorios y deben ser válidos.", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(restTemplate, never()).postForObject(anyString(), anyList(), eq(Boolean.class));
    }

    @Test
    void whenActualizarEstado_thenReturnUpdatedPedido() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(testPedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido updatedPedido = pedidoService.actualizarEstado(1L, "EN_CAMINO");

        assertNotNull(updatedPedido);
        assertEquals("EN_CAMINO", updatedPedido.getEstado());
        verify(pedidoRepository, times(1)).save(testPedido);
    }

    @Test
    void whenActualizarEstado_withNotFoundId_thenThrowRuntimeException() {
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.actualizarEstado(99L, "CANCELADO");
        });
        assertEquals("Pedido no encontrado con ID: 99", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void whenActualizarEstado_withNullEstado_thenThrowRuntimeException() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(testPedido));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.actualizarEstado(1L, null);
        });
        assertEquals("El nuevo estado no puede ser nulo o vacío.", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }


    @Test
    void whenObtenerPedidosPorCliente_thenReturnListOfPedidos() {
        when(pedidoRepository.findByClienteId(10L)).thenReturn(Arrays.asList(testPedido));

        List<Pedido> pedidos = pedidoService.obtenerPedidosPorCliente(10L);

        assertFalse(pedidos.isEmpty());
        assertEquals(1, pedidos.size());
        assertEquals(10L, pedidos.get(0).getClienteId());
        verify(pedidoRepository, times(1)).findByClienteId(10L);
    }

    @Test
    void whenObtenerPedidosPorCliente_withNoPedidos_thenReturnEmptyList() {
        when(pedidoRepository.findByClienteId(99L)).thenReturn(Collections.emptyList());

        List<Pedido> pedidos = pedidoService.obtenerPedidosPorCliente(99L);

        assertTrue(pedidos.isEmpty());
        verify(pedidoRepository, times(1)).findByClienteId(99L);
    }

    @Test
    void whenObtenerPedidosPorCliente_withInvalidClienteId_thenThrowRuntimeException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.obtenerPedidosPorCliente(0L);
        });
        assertEquals("El ID del cliente no puede ser nulo o inválido.", exception.getMessage());
        verify(pedidoRepository, never()).findByClienteId(anyLong());
    }


    @Test
    void whenObtenerPedidoPorId_thenReturnPedido() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(testPedido));

        Pedido foundPedido = pedidoService.obtenerPedidoPorId(1L);

        assertNotNull(foundPedido);
        assertEquals(1L, foundPedido.getPedidoId());
        verify(pedidoRepository, times(1)).findById(1L);
    }

    @Test
    void whenObtenerPedidoPorId_withNotFoundId_thenThrowRuntimeException() {
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.obtenerPedidoPorId(99L);
        });
        assertEquals("Pedido no encontrado con ID: 99", exception.getMessage());
        verify(pedidoRepository, times(1)).findById(99L);
    }

    @Test
    void whenObtenerPedidoPorId_withInvalidId_thenThrowRuntimeException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.obtenerPedidoPorId(0L);
        });
        assertEquals("El ID del pedido no puede ser nulo o inválido.", exception.getMessage());
        verify(pedidoRepository, never()).findById(anyLong());
    }

    @Test
    void whenDeletePedidoById_thenCallRepositoryDelete() {
        when(pedidoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(pedidoRepository).deleteById(1L);

        pedidoService.deletePedidoById(1L);

        verify(pedidoRepository, times(1)).existsById(1L);
        verify(pedidoRepository, times(1)).deleteById(1L);
    }

    @Test
    void whenDeletePedidoById_withNotFoundId_thenThrowRuntimeException() {
        when(pedidoRepository.existsById(anyLong())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.deletePedidoById(99L);
        });
        assertEquals("Pedido no encontrado con ID: 99", exception.getMessage());
        verify(pedidoRepository, times(1)).existsById(99L);
        verify(pedidoRepository, never()).deleteById(anyLong());
    }

    @Test
    void whenDeletePedidoById_withInvalidId_thenThrowRuntimeException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.deletePedidoById(0L);
        });
        assertEquals("El ID del pedido a eliminar no puede ser nulo o inválido.", exception.getMessage());
        verify(pedidoRepository, never()).existsById(anyLong());
        verify(pedidoRepository, never()).deleteById(anyLong());
    }
}