package co.unicauca.piedrazul.main;

import co.unicauca.piedrazul.infrastructure.persistence.PostgreSQLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

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
            Connection conn = PostgreSQLConnection.getConnection();

            if (conn != null && !conn.isClosed()) {
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
        }
    }

    @Override
    public void start(Stage stage) {
        Label label = new Label("Estado del Sistema: " + statusMessage);
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50;");

        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 500, 300);

        stage.setTitle("Piedrazul Medical Network - Cloud Connection Test");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
