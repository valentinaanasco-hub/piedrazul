package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.IRoleRepository;
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


    public boolean assignRole(int userId, String roleName) {
        // Verifica que el rol exista antes de asignarlo
        Role role = roleRepository.findByName(roleName);
        if (role == null)
            throw new IllegalArgumentException("Rol no encontrado");
        return roleRepository.assignRoleToUser(userId,role.getRoleId());
    }

    public List<Role> listRolesByUser(int userId) {
        // Para verificar permisos de un usuario
        return roleRepository.findRolesByUserId(userId);
    }
}
