package co.unicauca.piedrazul.presentation.controllers;

import co.unicauca.piedrazul.domain.entities.Role;
import co.unicauca.piedrazul.domain.entities.enums.RoleName;
import co.unicauca.piedrazul.domain.services.interfaces.IRoleService;
import java.util.ArrayList;
import java.util.List;

public class RoleController {
    private final IRoleService roleService;
    private String lastErrorMessage;

    public RoleController(IRoleService roleService) {
        this.roleService = roleService;
    }

    // Asigna un rol específico a una cuenta de usuario
    public boolean grantRole(int userId, RoleName name) {
        try {
            lastErrorMessage = null;
            return roleService.assignRole(userId, name);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return false;
        }
    }

    // Recupera la lista de roles que posee un usuario
    public List<Role> getRolesByUserId(int userId) {
        try {
            lastErrorMessage = null;
            return roleService.listRolesByUser(userId);
        } catch (Exception e) {
            lastErrorMessage = e.getMessage();
            return new ArrayList<>();
        }
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}