/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.acces;

import co.unicauca.piedrazul.domain.entities.Role;
import java.util.List;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public interface IRoleRepository {
    // Para crear un nuevo rol en el sistema
    boolean save(Role role);

    // Para buscar un rol por su id
    Role findById(int id);

    // Para buscar un rol por su nombre (ADMIN, DOCTOR, etc.)
    Role findByName(String name);

    // Para listar todos los roles disponibles
    List<Role> findAll();

    // Para asignar un rol a un usuario
    boolean assignRoleToUser(int userId, int roleId);

    // Para obtener todos los roles de un usuario específico
    List<Role> findRolesByUserId(int userId);
}
