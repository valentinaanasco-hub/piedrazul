package co.unicauca.piedrazul.appointment.domain.service.validator;

import co.unicauca.piedrazul.appointment.domain.model.Appointment;
import co.unicauca.piedrazul.appointment.domain.port.out.AppointmentRepositoryPort;
import co.unicauca.piedrazul.appointment.domain.port.out.DoctorSpecialtyPort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Valida que un paciente haya pasado por Consulta General antes de acceder a otros servicios.
 * Regla de negocio: todo paciente debe tener al menos una cita de Consulta General.
 * (Como todos los médicos ofrecen Consulta General, el requisito se cumple con cualquier
 *  médico; la guía de "primera cita = Consulta General" se refuerza en el frontend.)
 */
@Component
public class MedicinaGeneralValidator implements AppointmentValidator {

    private final AppointmentRepositoryPort repositoryPort;
    private final DoctorSpecialtyPort       doctorSpecialtyPort;

    public MedicinaGeneralValidator(AppointmentRepositoryPort repositoryPort,
                                    DoctorSpecialtyPort doctorSpecialtyPort) {
        this.repositoryPort      = repositoryPort;
        this.doctorSpecialtyPort = doctorSpecialtyPort;
    }

    @Override
    public void validate(Appointment appointment, List<Appointment> existingOnDate) {
        String specialty = doctorSpecialtyPort.getSpecialtyByDoctorId(appointment.getDoctorId());

        if ("Consulta General".equalsIgnoreCase(specialty)) {
            return;
        }

        List<Appointment> patientAppointments =
                repositoryPort.findByPatientIdOrderByDateDesc(appointment.getPatientId());

        boolean hasConsultaGeneral = false;
        for (Appointment past : patientAppointments) {
            if (past.getAppointmentId() == appointment.getAppointmentId()) {
                continue;
            }
            String pastSpecialty = doctorSpecialtyPort.getSpecialtyByDoctorId(past.getDoctorId());
            if ("Consulta General".equalsIgnoreCase(pastSpecialty)) {
                hasConsultaGeneral = true;
                break;
            }
        }

        if (!hasConsultaGeneral) {
            throw new IllegalArgumentException(
                    "Debes tener al menos una cita con Consulta General antes de acceder a otros servicios");
        }
    }
}
