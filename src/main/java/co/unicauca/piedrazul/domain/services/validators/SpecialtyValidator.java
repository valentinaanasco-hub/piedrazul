/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.validators;

import co.unicauca.piedrazul.domain.entities.Specialty;
import co.unicauca.piedrazul.domain.services.interfaces.ISpecialtyValidator;
import java.util.List;

/**
 *
 * @author santi
 */
public class SpecialtyValidator implements ISpecialtyValidator {
    
    @Override
    public void validateExists(Specialty specialty) {
        if (specialty == null) {
            throw new IllegalArgumentException("Especialidad no encontrada");
        }
    }

    @Override
    public void validateListNotEmpty(List<Specialty> specialties) {
        if (specialties == null || specialties.isEmpty()) {
            throw new IllegalArgumentException("No hay especialidades registradas");
        }
    }
}
