# Microservicio de Pedidos - Perfulandia SPA

## Descripción

Microservicio encargado de la gestión de pedidos para el sistema de Perfulandia SPA. Proporciona endpoints RESTful para crear, consultar y actualizar el estado de pedidos.

## Características principales

- Gestión completa de pedidos (creación, consulta, actualización)
- Integración con base de datos MySQL
- Documentación API con Swagger/OpenAPI
- Comunicación con otros microservicios mediante Feign
- Tolerancia a fallos con Resilience4j

## Tecnologías utilizadas

- **Lenguaje**: Java 11
- **Framework**: Spring Boot 2.7.5
- **Base de datos**: MySQL 8.0
- **Documentación API**: SpringDoc OpenAPI 1.6.15
- **Comunicación entre servicios**: Spring Cloud OpenFeign
- **Tolerancia a fallos**: Resilience4j
- **Build**: Maven

## Configuración inicial

### Requisitos previos

- Java 11 JDK instalado
- MySQL 8.0+ instalado y configurado
- Maven 3.6+

### Instalación

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/tu-usuario/pedidos-service.git
   ```

2. Configurar la base de datos:
   - Crear una base de datos llamada `Pedidos`
   - Configurar las credenciales en `application.properties`

3. Ejecutar la aplicación:
   ```bash
   mvn spring-boot:run
   ```

## Configuración de la base de datos

Editar el archivo `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/Pedidos
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Configuración HikariCP
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.maximum-pool-size=10
```

## Uso de la API

La aplicación estará disponible en `http://localhost:8080`

### Documentación de la API

Accede a la interfaz Swagger UI en:
```
http://localhost:8080/swagger-ui.html
```

### Endpoints principales

- `POST /pedidos` - Crear un nuevo pedido
- `GET /pedidos/{id}` - Obtener un pedido por ID
- `PUT /pedidos/{id}/estado` - Actualizar estado de un pedido
- `GET /pedidos/cliente/{clienteId}` - Obtener pedidos por cliente
- `GET /pedidos/sucursal/{sucursalId}` - Obtener pedidos por sucursal

## Estructura del proyecto

```
src/
├── main/
│   ├── java/
│   │   └── microservicio/
│   │       └── pedidos/
│   │           ├── config/          # Configuraciones
│   │           ├── controller/      # Controladores REST
│   │           ├── model/           # Entidades de dominio
│   │           ├── repository/      # Repositorios de datos
│   │           ├── service/         # Lógica de negocio
│   │           └── PedidoApplication.java  # Clase principal
│   └── resources/
│       ├── application.properties   # Configuración
│       └── static/                 # Recursos estáticos
└── test/                           # Pruebas
```

## Contribución

1. Haz fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Haz commit de tus cambios (`git commit -m 'Añadir nueva funcionalidad'`)
4. Haz push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## Contacto

Para consultas o soporte, contactar a [jorge.shakur.t@gmail.com](mailto:jorge.shakur.t@gmail.com)
