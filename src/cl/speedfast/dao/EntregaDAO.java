package cl.speedfast.dao;

import cl.speedfast.modelo.Entrega;
import cl.speedfast.util.ConexionDB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la entidad Entrega.
 * Las consultas de lectura incluyen un JOIN con pedidos y repartidores
 * para mostrar datos legibles en la tabla de la interfaz.
 */
public class EntregaDAO {

    /** Inserta una nueva entrega asociada a un pedido y un repartidor. */
    public void create(Entrega entrega) throws SQLException {
        String sql = "INSERT INTO entregas (id_pedido, id_repartidor, fecha, hora) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, entrega.getIdPedido());
            ps.setInt(2, entrega.getIdRepartidor());
            ps.setDate(3, Date.valueOf(entrega.getFecha()));
            ps.setTime(4, Time.valueOf(entrega.getHora()));
            ps.executeUpdate();
        }
    }

    /** Obtiene todas las entregas con la direccion del pedido y el nombre del repartidor. */
    public List<Entrega> readAll() throws SQLException {
        String sql =
                "SELECT e.id, e.id_pedido, e.id_repartidor, e.fecha, e.hora, "
                        + "       p.direccion, r.nombre "
                        + "FROM entregas e "
                        + "JOIN pedidos p ON e.id_pedido = p.id "
                        + "JOIN repartidores r ON e.id_repartidor = r.id "
                        + "ORDER BY e.id";

        List<Entrega> entregas = new ArrayList<>();

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                entregas.add(mapearEntrega(rs));
            }
        }
        return entregas;
    }

    /** Actualiza los datos de una entrega existente. */
    public void update(Entrega entrega) throws SQLException {
        String sql = "UPDATE entregas SET id_pedido = ?, id_repartidor = ?, "
                + "fecha = ?, hora = ? WHERE id = ?";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, entrega.getIdPedido());
            ps.setInt(2, entrega.getIdRepartidor());
            ps.setDate(3, Date.valueOf(entrega.getFecha()));
            ps.setTime(4, Time.valueOf(entrega.getHora()));
            ps.setInt(5, entrega.getId());
            ps.executeUpdate();
        }
    }

    /** Elimina una entrega por su id. */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM entregas WHERE id = ?";

        try (Connection conexion = ConexionDB.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Construye un objeto Entrega a partir de la fila actual del ResultSet. */
    private Entrega mapearEntrega(ResultSet rs) throws SQLException {
        Entrega entrega = new Entrega(
                rs.getInt("id"),
                rs.getInt("id_pedido"),
                rs.getInt("id_repartidor"),
                rs.getDate("fecha").toLocalDate(),
                rs.getTime("hora").toLocalTime()
        );
        entrega.setDireccionPedido(rs.getString("direccion"));
        entrega.setNombreRepartidor(rs.getString("nombre"));
        return entrega;
    }
}