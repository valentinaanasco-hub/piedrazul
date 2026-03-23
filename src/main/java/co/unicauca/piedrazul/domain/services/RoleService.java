/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.acces.IRoleRepository;
import co.unicauca.piedrazul.domain.entities.Role;
import java.util.List;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public class RoleService {
    private final IRoleRepository roleRepository;

    public RoleService(IRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public boolean registerRole(Role role) {
        // Valida que el nombre del rol no esté vacío
        if (role.getRoleName() == null || role.getRoleName().trim().isEmpty())
            throw new IllegalArgumentException("El nombre del rol es obligatorio");

        // Verifica que el rol no exista ya
        if (roleRepository.findByName(role.getRoleName()) != null)
            throw new IllegalArgumentException("El rol ya existe");

        return roleRepository.save(role);
    }

    public List<Role> listRoles() {
        return roleRepository.findAll();
    }

    public boolean assignRole(int userId, int roleId) {
        // Verifica que el rol exista antes de asignarlo
        if (roleRepository.findById(roleId) == null)
            throw new IllegalArgumentException("Rol no encontrado");
        return roleRepository.assignRoleToUser(userId, roleId);
    }

    public List<Role> listRolesByUser(int userId) {
        // Para verificar permisos de un usuario
        return roleRepository.findRolesByUserId(userId);
    }
}
