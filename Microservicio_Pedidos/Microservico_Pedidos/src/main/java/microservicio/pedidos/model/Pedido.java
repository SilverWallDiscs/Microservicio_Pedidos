
package microservicio.pedidos.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pedidoId;
    
    private Long clienteId;
    private Long sucursalId;
    private LocalDateTime fechaPedido;
    private LocalDateTime fechaEntregaEstimada;
    private String estado;
    private double total;
    private String direccionEnvio;
    private String metodoPago;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "pedido_id")
    private List<ItemPedido> items;
}
