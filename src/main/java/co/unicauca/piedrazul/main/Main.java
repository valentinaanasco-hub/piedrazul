
package co.unicauca.piedrazul.main;

import co.unicauca.piedrazul.infrastructure.persistence.SqliteUserRepository;
import co.unicauca.piedrazul.domain.access.IUserRepository;
import co.unicauca.piedrazul.domain.services.UserService;
import co.unicauca.piedrazul.presentation.view.LoginFrame;
import co.unicauca.piedrazul.presentation.view.RegisterFrame;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public class Main {

    public static void main(String[] args) {
        // 1. Iniciar la infraestructura (Conexión a SQLite)
        IUserRepository repository = new SqliteUserRepository();

        // 2. Iniciar la lógica de negocio (Servicio)
        UserService userService = new UserService(repository);
        
        // 3. Lanzar la interfaz gráfica (Login)
        java.awt.EventQueue.invokeLater(() -> {
            // Pasamos el servicio al frame para que el botón de inicio funcione
            RegisterFrame registerForm = new RegisterFrame(userService);
            registerForm.setLocationRelativeTo(null);
            registerForm.setVisible(true);
        });
    }
}