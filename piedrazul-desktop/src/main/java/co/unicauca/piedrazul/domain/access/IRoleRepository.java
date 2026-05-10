package co.unicauca.piedrazul.domain.access;

import co.unicauca.piedrazul.domain.entities.Role;
import co.unicauca.piedrazul.domain.entities.enums.RoleName;
import java.util.List;

/**
 *
 * @author santi
 */
public interface IRoleRepository {

    // Para asignar un rol a un usuario
    boolean assignRoleToUser(int userId, int roleId);

    //Encontrar un rol por su nombre
    Role findByName(RoleName name);

    // Para obtener todos los roles de un usuario específico
    List<Role> findRolesByUserId(int userId);
}
