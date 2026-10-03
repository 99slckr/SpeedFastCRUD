# SpeedFast - Sistema de Gestión de Entregas con Persistencia MySQL

Aplicación de escritorio en Java para la empresa de repartos **SpeedFast**, que gestiona repartidores, pedidos y entregas de forma persistente mediante una interfaz gráfica Swing conectada a una base de datos MySQL a través de JDBC.

## Descripción general

En las etapas anteriores el sistema SpeedFast resolvía su lógica de negocio en memoria: los datos existían solo mientras la aplicación estuviera en ejecución y se perdían al cerrarla.

Esta versión completa el ciclo funcional incorporando persistencia real. Las operaciones de creación, consulta, actualización y eliminación se ejecutan contra una base de datos MySQL, y la interfaz gráfica permite al usuario gestionar las tres entidades del sistema sin escribir una sola línea de SQL.

El acceso a datos se implementa mediante el patrón **DAO (Data Access Object)**, que aísla las consultas SQL en clases dedicadas. La interfaz no conoce la estructura de las tablas ni el motor de base de datos utilizado: solo invoca métodos. Esto permite modificar el esquema sin alterar la capa visual.

## Estructura de paquetes y clases

```
SpeedFastCRUD/
├── lib/
│   └── mysql-connector-j-26.7.0.jar   # Driver JDBC de MySQL
├── src/
│   └── cl/speedfast/
│       ├── Main.java                  # Punto de entrada
│       ├── modelo/                    # Entidades del dominio
│       │   ├── Repartidor.java
│       │   ├── Pedido.java
│       │   └── Entrega.java
│       ├── dao/                       # Acceso a datos (CRUD)
│       │   ├── RepartidorDAO.java
│       │   ├── PedidoDAO.java
│       │   └── EntregaDAO.java
│       ├── util/
│       │   └── ConexionDB.java        # Conexión centralizada a MySQL
│       └── vista/                     # Interfaz gráfica Swing
│           ├── VentanaPrincipal.java
│           ├── PanelRepartidores.java
│           ├── PanelPedidos.java
│           └── PanelEntregas.java
└── README.md
```

## Resumen de las clases principales

- **`util/ConexionDB`**: centraliza la URL, el usuario y la contraseña de la base de datos. Expone `obtenerConexion()`, método estático que devuelve una conexión nueva. Al estar definidos en un solo lugar, un cambio de credenciales no afecta al resto del sistema.
- **`modelo/Repartidor`**: entidad con `id` y `nombre`. Su `toString()` devuelve `"id - nombre"`, texto que se muestra en los combos de la interfaz.
- **`modelo/Pedido`**: entidad con `id`, `direccion`, `tipo` y `estado`. Los dos últimos corresponden a los valores ENUM definidos en la base de datos.
- **`modelo/Entrega`**: asocia un pedido con un repartidor mediante sus claves foráneas, más la fecha y hora de la entrega. Incluye además dos campos descriptivos (`direccionPedido` y `nombreRepartidor`) que no existen en la tabla y se obtienen por JOIN, para mostrar información legible en la tabla de la interfaz.
- **`dao/RepartidorDAO`, `PedidoDAO`, `EntregaDAO`**: implementan `create()`, `readAll()`, `update()` y `delete()` sobre su respectiva tabla. Todas las consultas usan `PreparedStatement` con parámetros, y las lecturas recorren un `ResultSet`. `PedidoDAO` agrega `readFiltrado()` para filtrar por estado y tipo; `EntregaDAO` resuelve sus lecturas con un JOIN hacia pedidos y repartidores.
- **`vista/VentanaPrincipal`**: `JFrame` que agrupa las tres pestañas. Detecta el cambio a la pestaña de entregas para recargar sus combos desde la base de datos.
- **`vista/PanelRepartidores`, `PanelPedidos`, `PanelEntregas`**: cada panel contiene su formulario, sus botones y su `JTable`. Validan la entrada, invocan al DAO correspondiente, informan el resultado mediante `JOptionPane` y refrescan la tabla tras cada operación.

## Funcionalidades del sistema

1. Registrar, editar y eliminar repartidores, listándolos en tabla.
2. Registrar, editar y eliminar pedidos con su dirección, tipo y estado.
3. Filtrar pedidos por estado, por tipo o por ambos criterios combinados.
4. Registrar entregas seleccionando pedido y repartidor desde combos cargados dinámicamente desde la base de datos.
5. Editar y eliminar entregas existentes.
6. Listar entregas mostrando la dirección del pedido y el nombre del repartidor en lugar de sus identificadores numéricos.
7. Cargar los datos de una fila al formulario al seleccionarla en la tabla.
8. Solicitar confirmación antes de cada eliminación.
9. Actualizar las tablas en tiempo real después de cada operación CRUD.

## Conceptos aplicados

- **Patrón DAO:** las sentencias SQL residen exclusivamente en el paquete `dao`. Ni la vista ni el modelo contienen consultas.
- **Separación por capas:** `modelo` define las entidades, `dao` resuelve la persistencia, `util` concentra la conexión y `vista` gestiona la interacción con el usuario.
- **`PreparedStatement`:** todos los valores viajan como parámetros `?` y nunca concatenados en el texto SQL, lo que previene inyección de SQL y permite al motor reutilizar el plan de ejecución.
- **`ResultSet`:** las consultas de lectura recorren el resultado fila por fila y construyen los objetos del modelo mediante métodos privados de mapeo, reutilizados por las distintas consultas de cada DAO.
- **Cierre de recursos:** todas las operaciones usan `try-with-resources`, de modo que la conexión, el `PreparedStatement` y el `ResultSet` se cierran automáticamente incluso si se lanza una excepción.
- **Manejo de excepciones por capas:** los DAO declaran `throws SQLException` sin capturarla, porque no pueden decidir cómo informar al usuario. La vista la captura y muestra un `JOptionPane` con un mensaje comprensible más el detalle técnico.
- **Validación de entrada:** los campos obligatorios se verifican antes de ejecutar cualquier operación, y la fecha y la hora se validan por formato capturando `DateTimeParseException`.
- **Integridad referencial:** las claves foráneas impiden eliminar un pedido o un repartidor con entregas asociadas. El sistema captura ese error y lo informa al usuario en lugar de fallar.
- **Combos con objetos:** los `JComboBox` almacenan objetos `Pedido` y `Repartidor` completos. Muestran el texto de su `toString()` pero conservan internamente el `id`, que es el valor que se persiste.

## Base de datos

El sistema utiliza el esquema `speedfast_db`:

```sql
CREATE DATABASE IF NOT EXISTS speedfast_db;
USE speedfast_db;

CREATE TABLE repartidores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL
);

CREATE TABLE pedidos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    direccion VARCHAR(100) NOT NULL,
    tipo ENUM('COMIDA','ENCOMIENDA','EXPRESS'),
    estado ENUM('PENDIENTE','EN_REPARTO','ENTREGADO')
);

CREATE TABLE entregas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido INT,
    id_repartidor INT,
    fecha DATE,
    hora TIME,
    FOREIGN KEY (id_pedido) REFERENCES pedidos(id),
    FOREIGN KEY (id_repartidor) REFERENCES repartidores(id)
);
```

## Cómo clonar y ejecutar el proyecto

### Clonar el repositorio

```
git clone https://github.com/99slckr/SpeedFastCRUD.git
cd SpeedFastCRUD
```

### Preparar la base de datos

1. Instalar MySQL Server 8.0 y MySQL Workbench.
2. Ejecutar el script SQL de la sección anterior para crear `speedfast_db` y sus tablas.
3. Ajustar las credenciales en `src/cl/speedfast/util/ConexionDB.java` si el usuario o la contraseña difieren de los valores por defecto (`root`).

### Desde IntelliJ IDEA

1. Abrir el proyecto en IntelliJ IDEA (**File → Open** y seleccionar la carpeta).
2. Verificar que `lib/mysql-connector-j-26.7.0.jar` esté agregado como librería (clic derecho sobre el archivo → **Add as Library...** si no lo estuviera).
3. Abrir `src/cl/speedfast/Main.java` y ejecutar el método `main`.

## Requisitos

- JDK 17 o superior.
- MySQL Server 8.0 o superior.
- MySQL Connector/J (incluido en `lib/`).

## Autor

- Autor: Andrés Acuña
- Asignatura: Desarrollo Orientado a Objetos II
- Actividad Sumativa - Semana 8