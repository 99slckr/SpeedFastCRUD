package cl.speedfast;

import cl.speedfast.util.ConexionDB;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Punto de entrada del sistema SpeedFast.
 * Por ahora verifica que la conexion con la base de datos funcione.
 */
public class Main {

    public static void main(String[] args) {
        try (Connection conexion = ConexionDB.obtenerConexion()) {
            System.out.println("Conexion exitosa con speedfast_db");
            System.out.println("Catalogo: " + conexion.getCatalog());
        } catch (SQLException e) {
            System.out.println("Error al conectar: " + e.getMessage());
        }
    }
}