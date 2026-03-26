package co.unicauca.piedrazul.domain.services.validators;

import co.unicauca.piedrazul.domain.entities.Appointment;
import co.unicauca.piedrazul.domain.entities.Doctor;
import co.unicauca.piedrazul.domain.entities.Patient;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import co.unicauca.piedrazul.domain.services.interfaces.IManualAppointmentValidator;

/**
 * @author Valentina Añasco
 * @author Camila Dorado
 * @author Felipe Gutierrez
 * @author Ginner Ortega
 * @author Santiago Solarte
 */
public class ManualAppointmentValidator implements IManualAppointmentValidator {

    @Override
    public void validate(Appointment appointment, Doctor doctor, Patient patient, List<Appointment> existing) {
        validateDate(appointment.getDate());
        validateTimes(appointment.getStartTime(), appointment.getEndTime());
        validateDoctor(doctor);
        validatePatient(patient);
        validateTimeConflict(appointment, existing);
    }

    @Override
    public void validateExists(Appointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("La cita no existe");
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        // La fecha debe ser estrictamente posterior a hoy
        if (date.isBefore(LocalDate.now()) || date.isEqual(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha debe ser al menos un día después de la fecha actual");
        }
    }

    private void validateTimes(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Las horas son obligatorias");
        }
        if (start.isAfter(end) || start.equals(end)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la de fin");
        }
    }

    private void validateDoctor(Doctor doctor) {
        if (doctor == null) {
            throw new IllegalArgumentException("El médico no existe");
        }
    }

    private void validatePatient(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("El paciente no existe");
        }
    }

    private void validateTimeConflict(Appointment appointment, List<Appointment> existing) {
        if (existing == null) {
            return;
        }

        for (Appointment a : existing) {
            // Ignorar la misma cita si se está reajustando
            if (a.getAppointmentId() == appointment.getAppointmentId()) {
                continue;
            }
            // Verifica colisión de hora de inicio
            if (appointment.getStartTime().equals(a.getStartTime())) {
                throw new IllegalArgumentException("El médico ya tiene una cita en ese horario");
            }
        }
    }
}
