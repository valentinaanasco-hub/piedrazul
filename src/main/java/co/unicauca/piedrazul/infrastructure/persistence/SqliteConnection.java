package co.unicauca.piedrazul.infrastructure.persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
/**
 *
 * @author Santiago Solarte
 */
public class SqliteConnection {
      private static final String URL = "jdbc:sqlite:piedrazul.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Método opcional para crear tablas automáticamente
    public static void initializeDatabase() {
         String sql = """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            primer_nombre TEXT NOT NULL,
            segundo_nombre TEXT,
            primer_apellido TEXT NOT NULL,
            segundo_apellido TEXT,
            username TEXT UNIQUE NOT NULL,
            password TEXT NOT NULL,
            correo TEXT NOT NULL,
            rol TEXT NOT NULL
        );
        """;

        try (Connection conn = getConnection();
         Statement stmt = conn.createStatement()) {
        stmt.execute(sql);
        System.out.println("Tabla users verificada/creada");
    } catch (SQLException e) {
        e.printStackTrace();
    }
    }
}
