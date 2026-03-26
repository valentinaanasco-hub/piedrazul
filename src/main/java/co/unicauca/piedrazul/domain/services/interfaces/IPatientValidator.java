/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package co.unicauca.piedrazul.domain.services.interfaces;

import co.unicauca.piedrazul.domain.entities.Patient;

/**
 *
 * @author santi
 */
public interface IPatientValidator {
    void validatePatient(Patient patient);
    void validateExists(Patient patient);
}
