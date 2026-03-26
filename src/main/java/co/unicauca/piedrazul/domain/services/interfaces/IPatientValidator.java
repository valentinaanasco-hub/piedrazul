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
