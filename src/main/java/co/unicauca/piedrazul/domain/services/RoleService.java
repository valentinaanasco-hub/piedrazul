package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.access.IRoleRepository;
import co.unicauca.piedrazul.domain.entities.Role;
import co.unicauca.piedrazul.domain.entities.enums.RoleName;
import co.unicauca.piedrazul.domain.services.interfaces.IRoleService;
import co.unicauca.piedrazul.domain.services.interfaces.IRoleValidator;
import java.util.List;

/**
 * @author Valentina Añasco 
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte 
 */

public class RoleService implements IRoleService {
    private final IRoleRepository roleRepository;
    private final IRoleValidator validator; // Inyectamos la abstracción

    public RoleService(IRoleRepository roleRepository, IRoleValidator validator) {
        this.roleRepository = roleRepository;
        this.validator = validator;
    }

    @Override
    public boolean assignRole(int userId, RoleName name) {
        // Buscamos el rol por su nombre (Enum)
        Role role = roleRepository.findByName(name);
        
        // El validador decide si el rol es válido para continuar
        validator.validateExists(role);
        
        // Procedemos con la operación en el repositorio
        return roleRepository.assignRoleToUser(userId, role.getRoleId());
    }

    @Override
    public List<Role> listRolesByUser(int userId) {
        List<Role> roles = roleRepository.findRolesByUserId(userId); 
        return roles;
    }
}
