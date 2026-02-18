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

    private static Connection instance;
    private static final String URL = "jdbc:sqlite:database/piedrazul.db";

    private SqliteConnection() {
    }

    public static Connection getConnection() {
        try {
            if (instance == null || instance.isClosed()) {
                instance = DriverManager.getConnection(URL);
                initDatabase(instance);
            }
        } catch (SQLException e) {
            System.err.println("Error de conexión SQLite: " + e.getMessage());
        }
        return instance;
    }

    private static void initDatabase(Connection con) {

        String createRoleTable = """
            CREATE TABLE IF NOT EXISTS "ROLE" (
                "ROLE_ID" INTEGER NOT NULL CHECK("ROLE_ID" > 0),
                "ROLE_NAME" TEXT CHECK("ROLE_NAME" IN ('MEDICO', 'TERAPISTA', 'AGENDADOR', 'ADMINISTRADOR')),
                PRIMARY KEY("ROLE_ID")
            );
            """;

        String createUserTable = """
            CREATE TABLE IF NOT EXISTS "USER" (
                "USER_ID" INTEGER NOT NULL CHECK("USER_ID" > 0),
                "USER_TYPE_ID" TEXT NOT NULL CHECK("USER_TYPE_ID" IN ('CC', 'TI', 'CE', 'PA')),
                "USER_FIRST_NAME" TEXT NOT NULL,
                "USER_MIDDLE_NAME" TEXT,
                "USER_FIRST_SURNAME" TEXT NOT NULL,
                "USER_LAST_NAME" TEXT,
                "USER_NAME" TEXT NOT NULL UNIQUE,
                "USER_PASSWORD" TEXT NOT NULL,
                "USER_STATE" TEXT NOT NULL DEFAULT 'ACTIVE' CHECK("USER_STATE" IN ('ACTIVE', 'INACTIVE')),
                "ROLE_ID" INTEGER,
                "EMAIL" TEXT NOT NULL,
                "BIRTH_DATE" TEXT NOT NULL,
                PRIMARY KEY("USER_ID"),
                FOREIGN KEY("ROLE_ID") REFERENCES "ROLE"("ROLE_ID")
            );
            """;

        String insertDefaultRoles = """
            INSERT OR IGNORE INTO "ROLE" ("ROLE_ID", "ROLE_NAME") VALUES
            (1, 'MEDICO'),
            (2, 'TERAPISTA'),
            (3, 'AGENDADOR'),
            (4, 'ADMINISTRADOR');
            """;

        try (Statement stmt = con.createStatement()) {
            stmt.execute(createRoleTable);
            stmt.execute(createUserTable);
            stmt.execute(insertDefaultRoles);
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }
}
