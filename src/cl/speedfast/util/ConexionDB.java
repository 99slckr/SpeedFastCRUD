package cl.speedfast.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centraliza la conexion con la base de datos MySQL del sistema.
 * Cualquier clase DAO obtiene su conexion a traves de esta clase,
 * de modo que los datos de acceso queden definidos en un solo lugar.
 */
public class ConexionDB {

    private static final String URL =
            "jdbc:mysql://localhost:3306/speedfast_db?useSSL=false&serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "admin1234";

    /**
     * Abre y devuelve una nueva conexion a la base de datos.
     *
     * @return una conexion activa con speedfast_db.
     * @throws SQLException si los datos de acceso son incorrectos
     *                      o el servidor MySQL no esta disponible.
     */
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
}