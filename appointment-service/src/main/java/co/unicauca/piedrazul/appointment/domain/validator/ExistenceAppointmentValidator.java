package co.unicauca.piedrazul.appointment.domain.validator;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import co.unicauca.piedrazul.appointment.domain.entities.UserCache;
import co.unicauca.piedrazul.appointment.domain.repository.UserCacheRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Valida que el paciente y el médico existan y estén activos
 */
@Component
public class ExistenceAppointmentValidator implements AppointmentValidator {

    private final UserCacheRepository userCacheRepository;

    public ExistenceAppointmentValidator(UserCacheRepository userCacheRepository) {
        this.userCacheRepository = userCacheRepository;
    }

    @Override
    public void validate(Appointment appointment, List<Appointment> existingOnDate) {
        UserCache patient = userCacheRepository.findById(appointment.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Paciente no encontrado con ID: " + appointment.getPatientId()));

        if (!patient.isActive()) {
            throw new IllegalArgumentException("El paciente está inactivo");
        }

        UserCache doctor = userCacheRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Médico no encontrado con ID: " + appointment.getDoctorId()));

        if (!doctor.isActive()) {
            throw new IllegalArgumentException("El médico está inactivo");
        }
    }
}
