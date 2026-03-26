package co.unicauca.piedrazul.main;

import co.unicauca.piedrazul.application.PiedrazulFacade;
import co.unicauca.piedrazul.infrastructure.persistence.PostgreSQLConnection;
import co.unicauca.piedrazul.presentation.views.MainView;
import co.unicauca.piedrazul.domain.entities.User;
import co.unicauca.piedrazul.domain.entities.enums.UserState;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    // Variable para rastrear el estado de la conexión
    private String statusMessage = "Iniciando...";

    @Override
    public void init() {
        // Lógica de inicialización (Conexión a la nube)
        System.out.println("🚀 Iniciando Piedrazul en la nube...");

        try {
            // 1. Intentar conectar a Railway
            Connection conn = PostgreSQLConnection.getConnection();

            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Conexión exitosa a Railway");

                // 2. Prueba de fuego: Insertar/Actualizar parámetro de prueba
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
        // 1. Definir el tipo de base de datos e inicializar la Fachada (Singleton)
        DataBaseType dbType = DataBaseType.POSTGRESQL;
        PiedrazulFacade.getInstance(dbType);

        // 2. Crear un usuario simulado (Simulando un Login exitoso)
        User testUser = new User(1, "CC", "Santiago", "", "Solarte", "", "santi", "123", UserState.ACTIVO, new ArrayList<>());

        // 3. Instanciar e inyectar dependencias hacia la vista principal
        // Nota: Asegúrate que el constructor de MainView acepte estos 4 parámetros
        MainView mainView = new MainView(stage, testUser, "AGENDADOR", dbType);
        
        // 4. Mostrar la ventana principal
        mainView.show();
        
        System.out.println("🖥️ Interfaz cargada. Estado: " + statusMessage);
    }

    public static void main(String[] args) {
        // Lanza la aplicación JavaFX (ejecuta init y luego start)
        launch(args);
    }
}