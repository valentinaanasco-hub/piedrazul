package co.unicauca.piedrazul.appointment.domain.validator;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Valida que no exista conflicto de horario para el médico en la fecha solicitada
 */
@Component
public class ConflictAppointmentValidator implements AppointmentValidator {

    @Override
    public void validate(Appointment appointment, List<Appointment> existingOnDate) {
        for (Appointment existing : existingOnDate) {
            boolean sameSlot = existing.getStartTime().equals(appointment.getStartTime());
            boolean differentAppointment = existing.getAppointmentId() != appointment.getAppointmentId();
            if (sameSlot && differentAppointment) {
                throw new IllegalArgumentException(
                        "Ya existe una cita para ese médico en esa fecha y hora");
            }
        }
    }
}
