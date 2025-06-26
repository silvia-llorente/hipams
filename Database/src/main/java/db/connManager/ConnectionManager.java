package db.connManager;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionManager {
    static String urlDB    = null;
    static String username = null;
    static String password = null;
    static String driver   = null;

    static Connection connection = null;

    static public Connection init_connection() {
        Properties prop = new Properties();

        // 1. Intentar cargar app.properties desde el classpath
        try (InputStream in = ConnectionManager.class
                                      .getClassLoader()
                                      .getResourceAsStream("app.properties")) {
            if (in == null) {
                System.err.println("No he encontrado app.properties en el classpath");
                return null;
            }
            prop.load(in);
            System.out.println("Propiedades leídas: "
                + prop.getProperty("db.url") + ", "
                + prop.getProperty("db.username"));
        } catch (IOException ex) {
            Logger.getLogger(ConnectionManager.class.getName())
                  .log(Level.SEVERE, "Error al leer app.properties", ex);
            return null;
        }

        // 2. Asignar valores cargados
        driver   = prop.getProperty("db.driver");   // e.g. com.mysql.cj.jdbc.Driver
        urlDB    = prop.getProperty("db.url");      // e.g. jdbc:mysql://localhost:3306/gipams_db
        username = prop.getProperty("db.username"); // e.g. SEARCH_user
        password = prop.getProperty("db.password"); // e.g. password

        System.out.println("driver=" + driver
                         + "  user=" + username
                         + "  pass=" + (password == null ? "null" : "***")
                         + "  url=" + urlDB);

        // 3. Comprobar que no falte ninguna propiedad
        if (driver == null || urlDB == null || username == null || password == null) {
            System.err.println("Falta alguna propiedad de conexión: "
                + "driver="   + driver
                + ", url="    + urlDB
                + ", user="   + username
                + ", pass="   + (password == null ? "null" : "***"));
            return null;
        }

        try {
            // 4. Cargar el driver de forma dinámica
            Class.forName(driver);

            // 5. Construir la URL completa añadiendo parámetros para SSL y public key retrieval
            String fullUrl = urlDB;
            if (!urlDB.contains("?")) {
                fullUrl += "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true";
            } else {
                fullUrl += "&autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true";
            }

            // 6. Intentar obtener la conexión
            connection = DriverManager.getConnection(fullUrl, username, password);
            System.out.println("Connection creada: " + connection);
            return connection;

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConnectionManager.class.getName())
                  .log(Level.SEVERE, "No se encontró el driver JDBC: " + driver, ex);
        } catch (SQLException e) {
            System.err.println("=== CONNECTION ERROR ===");
            e.printStackTrace(System.err);   // Muestra traza completa
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Error code: " + e.getErrorCode());
            System.err.println("Mensaje: " + e.getMessage());
        }

        return null;
    }

    static public void close_connection(Connection connect) {
        try {
            if (connect != null) {
                connect.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }
}
