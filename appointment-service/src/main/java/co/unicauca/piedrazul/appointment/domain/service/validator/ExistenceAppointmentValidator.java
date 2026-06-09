package co.unicauca.piedrazul.appointment.domain.service.validator;

import co.unicauca.piedrazul.appointment.domain.exception.AppointmentNotFoundException;
import co.unicauca.piedrazul.appointment.domain.exception.AppointmentValidationException;
import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.model.UserCache;
import co.unicauca.piedrazul.appointment.domain.port.out.UserValidationPort;

import java.util.List;

/**
 * Valida que el paciente y el médico existan y estén activos.
 * Clase pura de dominio — registrada como @Bean en AppConfig.
 */
public class ExistenceAppointmentValidator implements AppointmentValidator {

    private final UserValidationPort userValidationPort;

    public ExistenceAppointmentValidator(UserValidationPort userValidationPort) {
        this.userValidationPort = userValidationPort;
    }

    @Override
    public void validate(Appointment appointment, List<Appointment> existingOnDate) {
        UserCache patient = resolveUser(appointment.getPatientId(), "Paciente");
        if (!patient.isActive()) {
            throw new AppointmentValidationException("El paciente está inactivo");
        }

        UserCache doctor = resolveUser(appointment.getDoctorId(), "Médico");
        if (!doctor.isActive()) {
            throw new AppointmentValidationException("El médico está inactivo");
        }
    }

    private UserCache resolveUser(long userId, String role) {
        return userValidationPort.findUserById(userId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        role + " no encontrado con ID: " + userId));
    }
}
