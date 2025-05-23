package microservicio.pedidos.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "items_pedido")
public class ItemPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;
    
    private Long productoId;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
}