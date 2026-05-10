/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.interfaces;

import co.unicauca.piedrazul.domain.entities.Doctor;
import java.util.List;

/**
 *
 * @author santi
 */
public interface IDoctorValidator {
    void validateDoctor(Doctor doctor);
    void validateExists(Doctor doctor);
    void validateListNotEmpty(List<Doctor> doctors);
}
