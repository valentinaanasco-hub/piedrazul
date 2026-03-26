package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.IRoleRepository;
import co.unicauca.piedrazul.domain.entities.Role;
import co.unicauca.piedrazul.domain.entities.enums.RoleName;
import co.unicauca.piedrazul.domain.services.interfaces.IRoleValidator;
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
    private final IRoleValidator validator; // Inyectamos la abstracción

    public RoleService(IRoleRepository roleRepository, IRoleValidator validator) {
        this.roleRepository = roleRepository;
        this.validator = validator;
    }

    public boolean assignRole(int userId, RoleName name) {
        // 1. Buscamos el rol por su nombre (Enum)
        Role role = roleRepository.findByName(name);
        
        // 2. El validador decide si el rol es válido para continuar
        validator.validateExists(role);
        
        // 3. Procedemos con la operación en el repositorio
        return roleRepository.assignRoleToUser(userId, role.getRoleId());
    }

    public List<Role> listRolesByUser(int userId) {
        List<Role> roles = roleRepository.findRolesByUserId(userId);
        
        // Opcional: Validar si queremos que explote si no tiene roles
        // validator.validateListNotEmpty(roles);
        
        return roles;
    }
}
