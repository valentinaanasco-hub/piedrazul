package co.unicauca.piedrazul.appointment.domain.validator;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Valida que los datos básicos de la cita sean correctos:
 * fecha no nula, hora inicio anterior a hora fin, IDs positivos
 */
@Component
public class DataAppointmentValidator implements AppointmentValidator {

    @Override
    public void validate(Appointment appointment, List<Appointment> existingOnDate) {
        if (appointment.getDate() == null) {
            throw new IllegalArgumentException("La fecha de la cita es obligatoria");
        }
        if (appointment.getStartTime() == null || appointment.getEndTime() == null) {
            throw new IllegalArgumentException("La hora de inicio y fin son obligatorias");
        }
        if (!appointment.getStartTime().isBefore(appointment.getEndTime())) {
            throw new IllegalArgumentException(
                    "La hora de inicio debe ser anterior a la hora de fin");
        }
        if (appointment.getDoctorId() <= 0) {
            throw new IllegalArgumentException("El ID del médico debe ser positivo");
        }
        if (appointment.getPatientId() <= 0) {
            throw new IllegalArgumentException("El ID del paciente debe ser positivo");
        }
    }
}
