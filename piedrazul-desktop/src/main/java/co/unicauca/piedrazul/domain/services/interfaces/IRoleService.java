/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.interfaces;

import co.unicauca.piedrazul.domain.entities.Role;
import co.unicauca.piedrazul.domain.entities.enums.RoleName;
import java.util.List;

/**
 *
 * @author santi
 */
public interface IRoleService {
    // Vincula un rol específico (Admin, Médico, etc.) a un usuario
    boolean assignRole(int userId, RoleName name);

    // Recupera todos los roles que tiene asignados un usuario
    List<Role> listRolesByUser(int userId);
}
