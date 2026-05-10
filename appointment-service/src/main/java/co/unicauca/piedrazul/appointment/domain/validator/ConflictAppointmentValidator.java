package co.unicauca.piedrazul.appointment.domain.validator;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Valida que no exista conflicto de horario para el médico en la fecha solicitada.
 *
 * @author Santiago Solarte
 */
@Component
public class ConflictAppointmentValidator implements AppointmentValidator {

    @Override
    public void validate(Appointment appointment, List<Appointment> existingOnDate) {
        boolean conflict = existingOnDate.stream()
                .anyMatch(existing ->
                        existing.getAppointmentId() != appointment.getAppointmentId()
                        && existing.getStartTime().equals(appointment.getStartTime()));

        if (conflict) {
            throw new IllegalArgumentException(
                    "Ya existe una cita para ese médico en esa fecha y hora");
        }
    }
}
