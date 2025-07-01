package microservicio.comentarios;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import microservicio.pedidos.model.ItemPedido;
import microservicio.pedidos.model.Pedido;
import microservicio.pedidos.repository.PedidoRepository;
import microservicio.pedidos.service.PedidoService;
import org.springframework.web.client.RestTemplate;

public class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido testPedido;
    private ItemPedido testItem;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
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
    public void crearPedidoConDatosFaltantesTest() {
        testPedido.setClienteId(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.crearPedido(testPedido);
        });
        
        assertEquals("Los datos básicos del pedido (clienteId, sucursalId, items) son obligatorios.", exception.getMessage());
        verify(pedidoRepository, times(0)).save(any(Pedido.class));
        verify(restTemplate, times(0)).postForObject(anyString(), anyList(), eq(Boolean.class));
    }

    @Test
    public void actualizarPedidoCompletoNoExistenteTest() {
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.actualizarPedidoCompleto(99L, testPedido);
        });
        
        assertEquals("Pedido no encontrado con ID: 99", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    public void actualizarEstadoTest() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(testPedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido updatedPedido = pedidoService.actualizarEstado(1L, "EN_CAMINO");

        assertNotNull(updatedPedido);
        assertEquals("EN_CAMINO", updatedPedido.getEstado());
        verify(pedidoRepository, times(1)).save(testPedido);
    }

    @Test
    public void actualizarEstadoNoExistenteTest() {
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.actualizarEstado(99L, "CANCELADO");
        });
        
        assertEquals("Pedido no encontrado con ID: 99", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    public void actualizarEstadoNuloTest() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(testPedido));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.actualizarEstado(1L, null);
        });
        
        assertEquals("El nuevo estado no puede ser nulo o vacío.", exception.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    public void obtenerPedidosPorClienteTest() {
        when(pedidoRepository.findByClienteId(10L)).thenReturn(Arrays.asList(testPedido));

        List<Pedido> pedidos = pedidoService.obtenerPedidosPorCliente(10L);

        assertFalse(pedidos.isEmpty());
        assertEquals(1, pedidos.size());
        assertEquals(10L, pedidos.get(0).getClienteId());
        verify(pedidoRepository, times(1)).findByClienteId(10L);
    }

    @Test
    public void obtenerPedidosPorClienteNoExistenteTest() {
        when(pedidoRepository.findByClienteId(99L)).thenReturn(Collections.emptyList());

        List<Pedido> pedidos = pedidoService.obtenerPedidosPorCliente(99L);

        assertTrue(pedidos.isEmpty());
        verify(pedidoRepository, times(1)).findByClienteId(99L);
    }

    @Test
    public void obtenerPedidosPorClienteIdInvalidoTest() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.obtenerPedidosPorCliente(0L);
        });
        
        assertEquals("El ID del cliente no puede ser nulo o inválido.", exception.getMessage());
        verify(pedidoRepository, never()).findByClienteId(anyLong());
    }

    @Test
    public void obtenerPedidoPorIdTest() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(testPedido));

        Pedido foundPedido = pedidoService.obtenerPedidoPorId(1L);

        assertNotNull(foundPedido);
        assertEquals(1L, foundPedido.getPedidoId());
        verify(pedidoRepository, times(1)).findById(1L);
    }

    @Test
    public void obtenerPedidoPorIdNoExistenteTest() {
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.obtenerPedidoPorId(99L);
        });
        
        assertEquals("Pedido no encontrado con ID: 99", exception.getMessage());
        verify(pedidoRepository, times(1)).findById(99L);
    }

    @Test
    public void obtenerPedidoPorIdInvalidoTest() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.obtenerPedidoPorId(0L);
        });
        
        assertEquals("El ID del pedido no puede ser nulo o inválido.", exception.getMessage());
        verify(pedidoRepository, never()).findById(anyLong());
    }

    @Test
    public void eliminarPedidoPorIdTest() {
        when(pedidoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(pedidoRepository).deleteById(1L);

        pedidoService.deletePedidoById(1L);

        verify(pedidoRepository, times(1)).existsById(1L);
        verify(pedidoRepository, times(1)).deleteById(1L);
    }

    @Test
    public void eliminarPedidoPorIdNoExistenteTest() {
        when(pedidoRepository.existsById(anyLong())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.deletePedidoById(99L);
        });
        
        assertEquals("Pedido no encontrado con ID: 99", exception.getMessage());
        verify(pedidoRepository, times(1)).existsById(99L);
        verify(pedidoRepository, never()).deleteById(anyLong());
    }

    @Test
    public void eliminarPedidoPorIdInvalidoTest() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.deletePedidoById(0L);
        });
        
        assertEquals("El ID del pedido a eliminar no puede ser nulo o inválido.", exception.getMessage());
        verify(pedidoRepository, never()).existsById(anyLong());
        verify(pedidoRepository, never()).deleteById(anyLong());
    }
}