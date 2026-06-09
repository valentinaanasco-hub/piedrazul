package co.unicauca.piedrazul.appointment.domain.service.validator;

import co.unicauca.piedrazul.appointment.domain.exception.AppointmentConflictException;
import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.model.AppointmentStatus;
import co.unicauca.piedrazul.appointment.domain.port.out.AppointmentRepositoryPort;

import java.util.List;

/**
 * Valida que un paciente no tenga más de una cita activa (AGENDADA).
 * Regla de negocio: solo se permite 1 cita agendada por paciente a la vez.
 * Clase pura de dominio — registrada como @Bean en AppConfig.
 */
public class ActiveAppointmentValidator implements AppointmentValidator {

    private final AppointmentRepositoryPort repositoryPort;

    public ActiveAppointmentValidator(AppointmentRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public void validate(Appointment appointment, List<Appointment> existingOnDate) {
        // La regla solo aplica al CREAR una cita nueva (appointmentId == 0).
        // Al reagendar no se agrega otra cita activa.
        if (appointment.getAppointmentId() != 0) {
            return;
        }

        List<Appointment> activeAppointments = repositoryPort
                .findByPatientIdAndStatus(appointment.getPatientId(), AppointmentStatus.AGENDADA);

        if (!activeAppointments.isEmpty()) {
            throw new AppointmentConflictException(
                    "Ya tienes una cita agendada. Debes esperar a que sea atendida o cancelada antes de agendar otra");
        }
    }
}
