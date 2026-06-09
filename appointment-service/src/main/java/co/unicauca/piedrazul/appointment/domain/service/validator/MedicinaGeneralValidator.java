package co.unicauca.piedrazul.appointment.domain.service.validator;

import co.unicauca.piedrazul.appointment.domain.exception.AppointmentValidationException;
import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.model.PatientAuthorization;
import co.unicauca.piedrazul.appointment.domain.model.ServiceType;
import co.unicauca.piedrazul.appointment.domain.port.out.PatientAuthorizationPort;

import java.util.List;
import java.util.Optional;

/**
 * Valida el acceso a servicios especializados mediante autorización médica explícita.
 * Regla: sin autorización vigente del médico, el paciente solo puede agendar Consulta General.
 * Clase pura de dominio — registrada como @Bean en AppConfig.
 */
public class MedicinaGeneralValidator implements AppointmentValidator {

    private final PatientAuthorizationPort authorizationPort;

    public MedicinaGeneralValidator(PatientAuthorizationPort authorizationPort) {
        this.authorizationPort = authorizationPort;
    }

    @Override
    public void validate(Appointment appointment, List<Appointment> existingOnDate) {
        // Al reagendar no se re-valida (ya fue aprobado al crear)
        if (appointment.getAppointmentId() != 0) return;

        // Consulta General siempre permitida
        if (appointment.getServiceType() == ServiceType.CONSULTA_GENERAL) return;

        // Para cualquier servicio especializado: requiere autorización médica activa
        Optional<PatientAuthorization> auth =
                authorizationPort.findActiveByPatientId(appointment.getPatientId());

        boolean authorized = auth.isPresent()
                && auth.get().getServiceType() == appointment.getServiceType();

        if (!authorized) {
            throw new AppointmentValidationException(
                    "Para agendar " + appointment.getServiceType().name().replace('_', ' ').toLowerCase()
                    + " necesitas autorización médica vigente. "
                    + "Agenda una Consulta General para que el médico pueda autorizarte.");
        }
    }
}
