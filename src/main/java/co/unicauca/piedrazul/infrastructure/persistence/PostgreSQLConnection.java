package co.unicauca.piedrazul.infrastructure.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgreSQLConnection {

    private static Connection instance;
    
    //PatronSingleton
    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            // Datos de Railway (Se mantienen igual)
            String host = "centerbeam.proxy.rlwy.net";
            String port = "55609";
            String db = "railway";
            String user = "postgres";
            String pass = "IRAhYvIIaZGyJWHOUKqQqcjaEdAmtTXR";

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;

            try {
                Class.forName("org.postgresql.Driver");
                instance = DriverManager.getConnection(url, user, pass);
                System.out.println("Conexión exitosa a Railway");
            } catch (ClassNotFoundException e) {
                System.err.println("Error: No se encontró el Driver de PostgreSQL en el proyecto.");
                throw new SQLException(e);
            } catch (SQLException e) {
                System.err.println("Error al conectar con Railway: " + e.getMessage());
                throw e;
            }
        }
        return instance;
    }

    private static void initDatabase(Connection conn) {
        String[] sqlStatements = {
            // 1. Roles
            "CREATE TABLE IF NOT EXISTS roles ("
            + "role_id SERIAL, role_name VARCHAR(50) NOT NULL, "
            + "CONSTRAINT pk_roles PRIMARY KEY (role_id), "
            + "CONSTRAINT uk_role_name UNIQUE (role_name), "
            + "CONSTRAINT ck_role_name_valid CHECK (role_name IN ('ADMIN', 'DOCTOR', 'PACIENTE', 'AGENDADOR')))",
            // 2. Usuarios (ACTUALIZADO con user_type_id)
            "CREATE TABLE IF NOT EXISTS users ("
            + "user_id INTEGER, user_username VARCHAR(50) NOT NULL, user_password VARCHAR(255) NOT NULL, "
            + "user_first_name VARCHAR(100) NOT NULL, user_middle_name VARCHAR(100), "
            + "user_first_surname VARCHAR(100) NOT NULL, user_last_name VARCHAR(100), "
            + "user_state VARCHAR(20) DEFAULT 'ACTIVO', "
            + "user_type_id VARCHAR(5) NOT NULL, "
            + "CONSTRAINT pk_users PRIMARY KEY (user_id), "
            + "CONSTRAINT uk_user_username UNIQUE (user_username), "
            + "CONSTRAINT ck_user_id_positive CHECK (user_id > 0), "
            + "CONSTRAINT ck_user_type_id CHECK (user_type_id IN ('CC', 'TI', 'CE', 'PA', 'RC')), "
            + "CONSTRAINT ck_user_state_valid CHECK (user_state IN ('ACTIVO', 'INACTIVO')))",
            // 3. Intermedia Users_Roles
            "CREATE TABLE IF NOT EXISTS users_roles ("
            + "ur_user_id INTEGER NOT NULL, ur_role_id INTEGER NOT NULL, "
            + "CONSTRAINT pk_users_roles PRIMARY KEY (ur_user_id, ur_role_id), "
            + "CONSTRAINT fk_ur_user FOREIGN KEY (ur_user_id) REFERENCES users (user_id) ON DELETE CASCADE, "
            + "CONSTRAINT fk_ur_role FOREIGN KEY (ur_role_id) REFERENCES roles (role_id) ON DELETE CASCADE)",
            // 7. Pacientes
            "CREATE TABLE IF NOT EXISTS patients ("
            + "pat_user_id INTEGER, pat_phone VARCHAR(20) NOT NULL, pat_gender VARCHAR(20) NOT NULL, "
            + "pat_birth_day VARCHAR(2), pat_birth_month VARCHAR(2), pat_birth_year VARCHAR(4), pat_email VARCHAR(150), "
            + "CONSTRAINT pk_patients PRIMARY KEY (pat_user_id), "
            + "CONSTRAINT fk_pat_user FOREIGN KEY (pat_user_id) REFERENCES users (user_id) ON DELETE CASCADE, "
            + "CONSTRAINT ck_pat_gender_valid CHECK (pat_gender IN ('Hombre', 'Mujer', 'Otro')))",
            // Datos Iniciales (Roles)
            "INSERT INTO roles (role_name) SELECT 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'ADMIN')",
            "INSERT INTO roles (role_name) SELECT 'DOCTOR' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'DOCTOR')",
            "INSERT INTO roles (role_name) SELECT 'PACIENTE' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'PACIENTE')",
            "INSERT INTO roles (role_name) SELECT 'AGENDADOR' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'AGENDADOR')"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : sqlStatements) {
                stmt.execute(sql);
            }
            System.out.println("✅ Estructura sincronizada con el nuevo script.");
        } catch (SQLException e) {
            System.err.println("❌ Error al inicializar tablas: " + e.getMessage());
        }
    }
}
