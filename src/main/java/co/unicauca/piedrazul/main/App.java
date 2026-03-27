package co.unicauca.piedrazul.main;

import co.unicauca.piedrazul.infrastructure.persistence.PostgreSQLConnection;
import co.unicauca.piedrazul.presentation.controllers.LoginController;
import co.unicauca.piedrazul.presentation.views.LoginView;
import java.sql.Connection;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class App extends Application {

    private boolean isDatabaseConnected = false;

    @Override
    public void init() {
        System.out.println("[INFO] Inicializando servicios de Piedrazul...");
        try {
            Connection conn = PostgreSQLConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("[SUCCESS] Conexion establecida con el servidor de base de datos.");
                isDatabaseConnected = true;
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] No se pudo establecer la conexion: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage stage) {
        LoginView loginView = new LoginView();
        new LoginController(loginView);

        // Contenedor con scroll para prevenir recortes en pantallas pequeñas
        ScrollPane scrollPane = new ScrollPane(loginView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #f3f6fa; -fx-border-color: transparent;");

        Scene scene = new Scene(scrollPane, 550, 750);

        stage.setTitle("Piedrazul - Gestion de Citas Medicas");
        // El login suele ser fijo
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        if (isDatabaseConnected) {
            System.out.println("[INFO] Aplicacion lista para autenticacion.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
