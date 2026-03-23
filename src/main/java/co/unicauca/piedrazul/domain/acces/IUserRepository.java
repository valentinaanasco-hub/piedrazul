package co.unicauca.piedrazul.domain.acces;

import co.unicauca.piedrazul.domain.entities.User;
import java.util.List;

public interface IUserRepository {
    // Para el registro de nuevos usuarios
    boolean save(User user);
    
    // Para el inicio de sesión y validación de existencia
    User findByUsername(String username);
    
    // Para la gestión de usuarios (listarlos todos)
    List<User> findAll();
    
    // Para actualizar estado o datos del usuario
    boolean update(User user);
}

