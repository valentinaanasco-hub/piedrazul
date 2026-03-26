
package co.unicauca.piedrazul.domain.services.interfaces;

import co.unicauca.piedrazul.domain.entities.Appointment;
import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.entities.Patient;
import java.util.List;

/**
 *
 * @author santi
 */
public interface IManualAppointmentValidator {
    void validate(Appointment appointment, Doctor doctor, Patient patient, List<Appointment> existingAppointmentsOnDate);
    void validateExists(Appointment appointment);
}
