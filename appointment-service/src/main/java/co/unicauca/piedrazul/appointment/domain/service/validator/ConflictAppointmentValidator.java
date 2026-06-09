package co.unicauca.piedrazul.appointment.domain.service.validator;

import co.unicauca.piedrazul.appointment.domain.exception.AppointmentConflictException;
import co.unicauca.piedrazul.appointment.domain.model.Appointment;

import java.util.List;

/**
 * Valida que no exista conflicto de horario para el médico en la fecha solicitada.
 * Clase pura de dominio — registrada como @Bean en AppConfig.
 */
public class ConflictAppointmentValidator implements AppointmentValidator {

    @Override
    public void validate(Appointment appointment, List<Appointment> existingOnDate) {
        for (Appointment existing : existingOnDate) {
            boolean sameSlot              = existing.getStartTime().equals(appointment.getStartTime());
            boolean differentAppointment  = existing.getAppointmentId() != appointment.getAppointmentId();
            if (sameSlot && differentAppointment) {
                throw new AppointmentConflictException(
                        "Ya existe una cita para ese médico en esa fecha y hora");
            }
        }
    }
}
