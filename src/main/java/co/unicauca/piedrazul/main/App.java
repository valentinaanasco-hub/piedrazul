package co.unicauca.piedrazul.main;

import co.unicauca.piedrazul.infrastructure.persistence.PostgreSQLConnection;
import java.sql.Connection;
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
    
    @Override
    public void init(){
                System.out.println(" Iniciando Piedrazul...");

        try {
            // Obtiene la conexión e inicializa las tablas automáticamente
            Connection conn = PostgreSQLConnection.getConnection();

            if (conn != null && !conn.isClosed()) {
                System.out.println(" Conexión exitosa a PostgreSQL");
                
                System.out.println(" Tablas creadas correctamente");
                System.out.println(" Sistema listo");
            }

        } catch (SQLException e) {
            System.err.println(" Error: " + e.getMessage());
        }
    }
    @Override
    public void start(Stage stage) {
        var label = new Label("Hello Santiago, JavaFX.");
        var scene = new Scene(new StackPane(label), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
