package co.unicauca.piedrazul.domain.services;

import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.entities.Patient;
import java.time.LocalDate;

/**
 *
 * @author santi
 */
public class AppointmentValidator {
    //Permite cumplir Single Responsability Principle
    public void validateDate(LocalDate date) {

        // 1. Verifica que la fecha no sea nula
        if (date == null)
            throw new IllegalArgumentException("La fecha es obligatoria");

        // 2. Verifica que no sea en el pasado
        if (date.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("La fecha no puede ser en el pasado");
    }

    public void validateDoctor(Doctor doctor) {

        // 3. Verifica que exista
        if (doctor == null)
            throw new IllegalArgumentException("Médico no encontrado");

        // 4. Verifica que esté activo
        if (doctor.getState().equals("INACTIVO"))
            throw new IllegalArgumentException("El médico no está activo");
    }

    public void validatePatient(Patient patient) {

        // 5. Verifica que el paciente exista
        if (patient == null)
            throw new IllegalArgumentException("Paciente no encontrado");

        // 6. Verifica que esté activo
        if (patient.getState().equals("INACTIVO"))
            throw new IllegalArgumentException("El paciente no está activo");
    }
}
