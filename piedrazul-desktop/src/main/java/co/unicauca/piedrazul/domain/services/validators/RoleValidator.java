/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.validators;

import co.unicauca.piedrazul.domain.entities.Role;
import co.unicauca.piedrazul.domain.services.interfaces.IRoleValidator;
import java.util.List;

/**
 *
 * @author santi
 */
public class RoleValidator implements IRoleValidator {
    @Override
    public void validateExists(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("El rol especificado no existe en el sistema");
        }
    }

    @Override
    public void validateListNotEmpty(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("El usuario no tiene roles asignados");
        }
    }
}
