package co.unicauca.piedrazul.main;
/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */
import co.unicauca.piedrazul.infrastructure.persistence.SqliteConnection;
import java.sql.Connection;
import co.unicauca.piedrazul.presentation.view.LoginFrame;
        import co.unicauca.piedrazul.presentation.view.RegisterFrame;

public class Main {
    public static void main(String[] args) {

      SqliteConnection.initializeDatabase();

    java.awt.EventQueue.invokeLater(() -> new LoginFrame().setVisible(true));

 
    }
}