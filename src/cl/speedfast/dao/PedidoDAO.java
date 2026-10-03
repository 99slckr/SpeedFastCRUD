package cl.speedfast.dao;

import cl.speedfast.modelo.Pedido;
import cl.speedfast.util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la entidad Pedido.
 * Implementa las operaciones CRUD sobre la tabla pedidos.
 */
public class PedidoDAO {

    /** Inserta un nuevo pedido en la base de datos. */
    public void create(Pedido pedido) throws SQLException {
        String sql = "INSERT INTO pedidos (direccion, tipo, estado) VALUES (?, ?, ?)";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, pedido.getDireccion());
            ps.setString(2, pedido.getTipo());
            ps.setString(3, pedido.getEstado());
            ps.executeUpdate();
        }
    }

    /** Obtiene todos los pedidos registrados, ordenados por id. */
    public List<Pedido> readAll() throws SQLException {
        String sql = "SELECT id, direccion, tipo, estado FROM pedidos ORDER BY id";
        List<Pedido> pedidos = new ArrayList<>();

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                pedidos.add(mapearPedido(rs));
            }
        }
        return pedidos;
    }

    /**
     * Obtiene los pedidos filtrados por estado o por tipo.
     * Un filtro en null o vacio se ignora, permitiendo combinar o no ambos criterios.
     */
    public List<Pedido> readFiltrado(String estado, String tipo) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id, direccion, tipo, estado FROM pedidos WHERE 1 = 1");
        List<String> parametros = new ArrayList<>();

        if (estado != null && !estado.isBlank()) {
            sql.append(" AND estado = ?");
            parametros.add(estado);
        }
        if (tipo != null && !tipo.isBlank()) {
            sql.append(" AND tipo = ?");
            parametros.add(tipo);
        }
        sql.append(" ORDER BY id");

        List<Pedido> pedidos = new ArrayList<>();

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setString(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapearPedido(rs));
                }
            }
        }
        return pedidos;
    }

    /** Actualiza los datos de un pedido existente. */
    public void update(Pedido pedido) throws SQLException {
        String sql = "UPDATE pedidos SET direccion = ?, tipo = ?, estado = ? WHERE id = ?";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, pedido.getDireccion());
            ps.setString(2, pedido.getTipo());
            ps.setString(3, pedido.getEstado());
            ps.setInt(4, pedido.getId());
            ps.executeUpdate();
        }
    }

    /** Elimina un pedido por su id. */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM pedidos WHERE id = ?";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Construye un objeto Pedido a partir de la fila actual del ResultSet. */
    private Pedido mapearPedido(ResultSet rs) throws SQLException {
        return new Pedido(
                rs.getInt("id"),
                rs.getString("direccion"),
                rs.getString("tipo"),
                rs.getString("estado")
        );
    }
}
