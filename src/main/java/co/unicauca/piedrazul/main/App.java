package co.unicauca.piedrazul.main;

import co.unicauca.piedrazul.domain.entities.User;
import co.unicauca.piedrazul.infrastructure.persistence.PostgreSQLConnection;
import co.unicauca.piedrazul.presentation.views.MainView;
import java.sql.Connection;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void init() {
        System.out.println("Iniciando Piedrazul...");
        try {
            // Obtiene la conexión — PostgreSQLConnection crea las tablas
            // automáticamente en initDatabase() si no existen (Singleton)
            Connection conn = PostgreSQLConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Conexión exitosa a PostgreSQL");
            }
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            // La app puede seguir sin BD (mostrará errores al intentar cargar datos)
        }
    }

    @Override
    public void start(Stage stage) {
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
    }

    public static void main(String[] args) {
        launch();
    }
}