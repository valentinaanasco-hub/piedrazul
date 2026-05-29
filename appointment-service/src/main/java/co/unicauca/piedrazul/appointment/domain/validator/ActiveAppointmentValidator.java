package co.unicauca.piedrazul.appointment.domain.validator;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import co.unicauca.piedrazul.appointment.domain.entities.enums.AppointmentStatus;
import co.unicauca.piedrazul.appointment.domain.repository.AppointmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Valida que un paciente no tenga más de una cita activa (AGENDADA)
 * Regla de negocio: solo se permite 1 cita agendada por paciente a la vez
 */
@Component
public class ActiveAppointmentValidator implements AppointmentValidator {

    private final AppointmentRepository appointmentRepository;

    public ActiveAppointmentValidator(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public void validate(Appointment appointment, List<Appointment> existingOnDate) {
        // Solo validar si es una cita nueva (ID = 0) o si está en estado AGENDADA
        if (appointment.getAppointmentId() == 0 || 
            appointment.getStatus() == AppointmentStatus.AGENDADA) {
            
            List<Appointment> activeAppointments = 
                appointmentRepository.findByPatientIdAndStatus(
                    appointment.getPatientId(), 
                    AppointmentStatus.AGENDADA
                );

            // Filtrar la cita actual si es un reagendamiento
            for (Appointment active : activeAppointments) {
                if (active.getAppointmentId() != appointment.getAppointmentId()) {
                    throw new IllegalArgumentException(
                        "Ya tienes una cita agendada. Debes esperar a que sea atendida o cancelada antes de agendar otra");
                }
            }
        }
    }
}
