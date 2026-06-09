package co.unicauca.piedrazul.appointment.domain.service.validator;

import co.unicauca.piedrazul.appointment.domain.exception.AppointmentValidationException;
import co.unicauca.piedrazul.appointment.domain.model.Appointment;

import java.util.List;

/**
 * Valida que los datos básicos de la cita sean correctos:
 * fecha no nula, hora inicio anterior a hora fin, IDs positivos.
 * Clase pura de dominio — registrada como @Bean en AppConfig.
 */
public class DataAppointmentValidator implements AppointmentValidator {

    @Override
    public void validate(Appointment appointment, List<Appointment> existingOnDate) {
        if (appointment.getDate() == null) {
            throw new AppointmentValidationException("La fecha de la cita es obligatoria");
        }
        if (appointment.getStartTime() == null || appointment.getEndTime() == null) {
            throw new AppointmentValidationException("La hora de inicio y fin son obligatorias");
        }
        if (!appointment.getStartTime().isBefore(appointment.getEndTime())) {
            throw new AppointmentValidationException(
                    "La hora de inicio debe ser anterior a la hora de fin");
        }
        if (appointment.getDoctorId() <= 0) {
            throw new AppointmentValidationException("El ID del médico debe ser positivo");
        }
        if (appointment.getPatientId() <= 0) {
            throw new AppointmentValidationException("El ID del paciente debe ser positivo");
        }
    }
}
