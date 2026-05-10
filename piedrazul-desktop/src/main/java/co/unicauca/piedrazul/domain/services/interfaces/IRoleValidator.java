/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.interfaces;

import co.unicauca.piedrazul.domain.entities.Role;
import java.util.List;

/**
 *
 * @author santi
 */
public interface IRoleValidator {
    void validateExists(Role role);
    void validateListNotEmpty(List<Role> roles);
}
