package co.unicauca.piedrazul.appointment.domain.validator;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import co.unicauca.piedrazul.appointment.domain.repository.AppointmentRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Valida que un paciente haya pasado por Medicina General antes de acceder a especialidades
 * Regla de negocio: todo paciente debe tener al menos una cita con Medicina General
 */
@Component
public class MedicinaGeneralValidator implements AppointmentValidator {

    private final AppointmentRepository appointmentRepository;
    private final RestTemplate restTemplate;
    private final String medicalStaffServiceUrl = "http://medical-staff-service:8082";

    public MedicinaGeneralValidator(AppointmentRepository appointmentRepository,
                                     RestTemplate restTemplate) {
        this.appointmentRepository = appointmentRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public void validate(Appointment appointment, List<Appointment> existingOnDate) {
        // Obtener especialidad del médico
        String specialty = getDoctorSpecialty(appointment.getDoctorId());
        
        // Si es Medicina General, permitir siempre
        if ("Medicina General".equalsIgnoreCase(specialty)) {
            return;
        }

        // Si es especialidad, verificar que el paciente tenga citas previas con Medicina General
        List<Appointment> patientAppointments = 
            appointmentRepository.findByPatientIdOrderByDateDescStartTimeAsc(appointment.getPatientId());

        boolean hasMedicinaGeneral = false;
        for (Appointment past : patientAppointments) {
            // Ignorar la cita actual si es un reagendamiento
            if (past.getAppointmentId() == appointment.getAppointmentId()) {
                continue;
            }
            
            String pastSpecialty = getDoctorSpecialty(past.getDoctorId());
            if ("Medicina General".equalsIgnoreCase(pastSpecialty)) {
                hasMedicinaGeneral = true;
                break;
            }
        }

        if (!hasMedicinaGeneral) {
            throw new IllegalArgumentException(
                "Debes tener al menos una cita con Medicina General antes de acceder a especialidades");
        }
    }

    /**
     * Obtiene la especialidad principal de un médico consultando el servicio de personal médico
     */
    private String getDoctorSpecialty(int doctorId) {
        try {
            String url = medicalStaffServiceUrl + "/api/v1/medical/doctors/" + doctorId;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("specialties")) {
                List<String> specialties = (List<String>) response.get("specialties");
                if (specialties != null && !specialties.isEmpty()) {
                    return specialties.get(0);
                }
            }
            return "Desconocida";
        } catch (Exception e) {
            // Si falla la consulta, permitir la cita (fail-safe)
            return "Medicina General";
        }
    }
}
