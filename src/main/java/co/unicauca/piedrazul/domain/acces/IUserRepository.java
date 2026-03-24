package co.unicauca.piedrazul.domain.acces;

import co.unicauca.piedrazul.domain.entities.User;
import java.util.List;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public interface IUserRepository {
    
    // Para el inicio de sesión y validación de existencia
    User findByUsername(String username);
    
    // Para la gestión de usuarios (listarlos todos)
    List<User> findAll();
    
    // Para el registro de nuevos usuarios
    boolean save(User user);

    
    // Para actualizar estado o datos del usuario
    boolean update(User user);
    
    // Para desactivar un paciente sin eliminar su historial
    boolean desactivate(int id);
}

