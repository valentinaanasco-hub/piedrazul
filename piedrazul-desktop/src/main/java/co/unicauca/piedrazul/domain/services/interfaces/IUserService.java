/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.interfaces;

import co.unicauca.piedrazul.domain.entities.User;
import java.util.List;

/**
 *
 * @author santi
 */
public interface IUserService {
    // Registra un nuevo usuario en el sistema
    boolean registerUser(User user);

    // Valida credenciales y retorna el usuario si el acceso es concedido
    User login(String username, String password);

    // Lista todos los usuarios registrados
    List<User> listUsers();

    // Busca un usuario por su nombre de cuenta
    User findUser(String username);

    // Modifica los datos de perfil de un usuario
    boolean modifyUser(User user);

    // Deshabilita el acceso de un usuario al sistema
    boolean deactivateUser(int id);
}
