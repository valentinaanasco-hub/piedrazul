package co.unicauca.piedrazul.main;

import co.unicauca.piedrazul.domain.entities.User;
import co.unicauca.piedrazul.infrastructure.persistence.PostgreSQLConnection;
import co.unicauca.piedrazul.presentation.views.MainView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.stage.Stage;

<<<<<<< HEAD
public class App extends Application {

    @Override
    public void init() {
        System.out.println("Iniciando Piedrazul...");
        try {
            // Obtiene la conexión — PostgreSQLConnection crea las tablas
            // automáticamente en initDatabase() si no existen (Singleton)
=======
/**
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
public class App extends Application {

    private String statusMessage = "Iniciando...";

    @Override
    public void init() {
        System.out.println("🚀 Iniciando Piedrazul en la nube...");

        try {
            // 1. Intentar conectar e inicializar tablas
>>>>>>> 6fb7147cd98ee6a449b068c01ae75b7eef3f0e49
            Connection conn = PostgreSQLConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
<<<<<<< HEAD
                System.out.println("Conexión exitosa a PostgreSQL");
            }
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            // La app puede seguir sin BD (mostrará errores al intentar cargar datos)
=======
                System.out.println("✅ Conexión exitosa a Railway");

                // 2. Prueba de fuego: Insertar un parámetro de sistema de prueba
                String testSql = "INSERT INTO system_parameters (parameter_key, parameter_value) "
                        + "VALUES (?, ?) ON CONFLICT (parameter_key) DO UPDATE SET parameter_value = EXCLUDED.parameter_value";

                try (PreparedStatement pstmt = conn.prepareStatement(testSql)) {
                    pstmt.setString(1, "last_connection_test");
                    pstmt.setString(2, "Exitosa desde el PC de " + System.getProperty("user.name"));
                    pstmt.executeUpdate();
                    System.out.println("💾 Datos de prueba guardados en la nube.");
                }

                statusMessage = "Conectado a PostgreSQL (Railway)";
            }
        } catch (SQLException e) {
            statusMessage = "Error de conexión: " + e.getMessage();
            System.err.println("❌ " + statusMessage);
>>>>>>> 6fb7147cd98ee6a449b068c01ae75b7eef3f0e49
        }
    }

    @Override
    public void start(Stage stage) {
<<<<<<< HEAD
        // ── Usuario de sesión simulado ────────────────────────────────────────
        // Representa el usuario que inició sesión
        User loggedUser = new User();
        loggedUser.setId(1);
        loggedUser.setFirstName("Admin");
        loggedUser.setFirstSurname("Sistema");
        loggedUser.setUsername("admin");

        // Rol del usuario: controla qué vistas puede ver
        // Cambiar aquí para probar diferentes roles
        String loggedUserRole = "AGENDADOR";

        // ── Lanza la ventana principal ────────────────────────────────────────
        // MainView ensambla todos los repositorios, servicios y controladores
        MainView mainView = new MainView(stage, loggedUser, loggedUserRole);
        mainView.show();
=======
        Label label = new Label("Estado del Sistema: " + statusMessage);
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50;");

        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 500, 300);

        stage.setTitle("Piedrazul Medical Network - Cloud Connection Test");
        stage.setScene(scene);
        stage.show();
>>>>>>> 6fb7147cd98ee6a449b068c01ae75b7eef3f0e49
    }

    public static void main(String[] args) {
        launch(args);
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 6fb7147cd98ee6a449b068c01ae75b7eef3f0e49
