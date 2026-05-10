/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.validators;

import co.unicauca.piedrazul.domain.entities.SystemParameter;
import co.unicauca.piedrazul.domain.services.interfaces.ISystemParameterValidator;

/**
 *
 * @author santi
 */
public class SystemParameterValidator implements ISystemParameterValidator {
    
    @Override
    public void validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("La llave del parámetro no puede estar vacía");
        }
    }

    @Override
    public void validateExists(SystemParameter parameter, String key) {
        if (parameter == null) {
            throw new IllegalArgumentException("El parámetro del sistema con la llave '" + key + "' no fue encontrado");
        }
    }
}
